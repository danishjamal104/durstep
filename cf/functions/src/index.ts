import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
admin.initializeApp()
const month = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 
'NOV', 'DEC']
export const notify = 
functions.https.onRequest((req, res)=>{
    //https://us-central1-durstep-7e7a8.cloudfunctions.net/notify?to=gre4OYbgiEZb5StRpOAldyVoZwD2&title=HttpText&msg=Hello
    const to = ""+req.query.to
    const title= ""+req.query.title
    let message = ""+req.query.msg
    const type = ""+req.query.type

    admin.firestore().doc(`user/${to}`).get()
    .then(ud => {
        const push_token = ud.data()?.push_token
        if(push_token===null){
            return res.send("No token found");
        }
        if(type==="1"){
            // if type is 1 then user must want to mention his name and number in message
            // hence provide is as extra query 
            message += " "+req.query.extra
        }
        const payload = {
            token: push_token,
            notification: {
                  title: title,
                  body: message
                }
            }
        return admin.messaging().send(payload)
        .then(()=>{
            return res.send('Send')
        })
        .catch((error) => {return res.send(error)})
    }).catch((error) => {return res.send(error)})
})
export const scan = 
functions.https.onRequest((req, res) => {
//https://us-central1-durstep-7e7a8.cloudfunctions.net/scan?to=

    //https:<domain>.com/scan?to={uId}&l={litreAmount}
    const to = req.query.to
    const amountSt = req.query.l
    const amount: number = parseFloat(<string>amountSt)
    admin.firestore().collection(`user/${to}/subscriptions/`).orderBy('sDate', 'desc').get()
    .then(querySnap => {
        // complete function by getting data
        if(querySnap.empty){
            return res.send({status: 202,msg: 'No Subscription',data: null})
        }
        const docs = querySnap.docs
        let active_sub
        let isAnyActive = false
        for (let i = 0; i < docs.length; i++) {
            const element = docs[i].data();
            const active = element.active
            if(active!==null){
                isAnyActive = true
                active_sub = element
                break
            }
        }
        console.log(`Variable isAnyActive: ${isAnyActive}`)
        if(!isAnyActive){
            return res.send({status: 202,msg: 'No Active Delivery',data: null})
        }
        console.log(`Variable past if condition isAnyActive: ${isAnyActive}`)
        return (<admin.firestore.DocumentReference> active_sub?.active).get()
        .then(adt => {
            //adt = active_delivery 
            const dt = adt.data()
            const subscription_list: Array<admin.firestore.DocumentReference> = 
            dt?.subscription_list
            const order = {
                to: to,
                from: adt.id,
                amount: amount,
                time: admin.firestore.Timestamp.now(),
                feedback: null,
            }
            return admin.firestore().collection('orders').add(order)
            .then(()=>{
                // till here order is created
                // next step is to update active_delivery
                const subs_ref: admin.firestore.DocumentReference = 
                admin.firestore().doc(`user/${to}/subscriptions/${active_sub?.sId}`)
                let idx = -1

                for (let i = 0; i < subscription_list.length; i++) {
                    const id_from_dist = subscription_list[i].id
                    const id_from_user = subs_ref.id
                    if(id_from_dist===id_from_user){
                        idx = i
                        break
                    }
                }
                if(idx>-1){
                    subscription_list.splice(idx, 1)
                }
                const delivered_list: Array<admin.firestore.DocumentReference> = 
                dt?.delivered_list
                delivered_list.push(subs_ref)
                const newDt = {
                    subscription_list: subscription_list,
                    delivered_list: delivered_list,
                    total: dt?.total,
                    pending: dt?.pending-1,
                    delivered: dt?.delivered+1,
                    location: dt?.location
                }
                let isDeliveryPending = true
                let promise: any = []
                if(newDt.pending==0){
                    isDeliveryPending = false
                    promise.push(adt.ref.delete())
                }else{
                    promise.push(adt.ref.update(newDt))
                }

                promise.push(
                    admin.firestore().doc(`user/${to}/subscriptions/${active_sub?.sId}`)
                    .update('active', null))

                return Promise.all(promise)
                .then(()=>{
                    return res.send({status: isDeliveryPending?200:204,msg: 'Order Delivered',data: adt.id})
                }).catch((e) => {console.log(e)
                    return res.send({status: 404,msg: 'Error Updating Active Delivery!! and  Setting Active To Null!!',data: e})})
            }) .catch((e) => {console.log(e)
                return res.send({status: 404,msg: 'Error Creating Order!!',data: e})})
        }).catch((e) => {
            console.log(e)
            return res.send({status: 404,msg: 'Delivery Not Found!!',data: e})})
    }).catch((e) => {
        console.log(e)
        return res.send({status: 404,msg: 'Invalid User!!',data: e})})
})
export const onNewDeliveryCreated = 
functions.firestore.document('active_delivery/{distributorId}')
.onCreate(dt => {
    const ad = dt.data()
    const ref: admin.firestore.DocumentReference = 
    admin.firestore().doc(`active_delivery/${dt.id}`)
    return admin.firestore().doc(`user/${dt.id}`).get()
    .then((distData) => {
        //const distributor = distData.data()
        const subs_promises: any = []
        const subs_list: Array<admin.firestore.DocumentReference> = ad?.subscription_list
        subs_list.forEach(element => {
            subs_promises.push(
                element.update('active', ref)
            )
        })
        return Promise.all(subs_promises)
    }).catch(error => console.log(error))
})
export const sortOrder = 
functions.firestore.document('orders/{oId}')
.onCreate(dt => {
    

    const order = dt.data()
    const time: Date = (<admin.firestore.Timestamp>order.time).toDate()

    const localTime = time.getTime()
    const localOffset = time.getTimezoneOffset() * 60000
    const utc = localTime+localOffset
    const delhi = utc+(3600000*5.5)
    const date_in_india = new Date(delhi)

    const doc_name = month[date_in_india.getMonth()]+'_'+date_in_india.getFullYear()
    //console.log(doc_name)

    return admin.firestore().doc('admin/meta-data').get()
    .then(snap => {
        const rate: number = snap.data()?.rate

        const stats_promises: Promise<admin.firestore.WriteResult>[] = []

        const client_stats_data = {
            due: admin.firestore.FieldValue.increment(rate*order.amount),
            orders: admin.firestore.FieldValue.arrayUnion(dt.ref),
            consumption: admin.firestore.FieldValue.increment(order.amount)
        }

        const admin_stats_data = {
            consumption: admin.firestore.FieldValue.increment(order.amount),
            total_amount: admin.firestore.FieldValue.increment(rate*order.amount),
            orders: admin.firestore.FieldValue.arrayUnion(dt.ref),
        }
        const disttributor_stats_data = {
            litre_delivered: admin.firestore.FieldValue.increment(order.amount),
            orders: admin.firestore.FieldValue.arrayUnion(dt.ref)
        }

        stats_promises.push(admin.firestore().doc(`user/X1XXmSzO1PVgRcDU69j9F8PLi1V2/stats/${doc_name}`)
        .set(admin_stats_data, {merge:true}))
        stats_promises.push(admin.firestore().doc(`user/${order.from}/stats/${doc_name}`)
        .set(disttributor_stats_data, {merge: true}))
        stats_promises.push(admin.firestore().doc(`user/${order.to}/stats/${doc_name}`)
        .set(client_stats_data, {merge: true}))

        return Promise.all(stats_promises)
        .catch((e) => {console.log(e)})
    }).catch((e) => {console.log(e)})
})
export const onActiveDeliveryDeleted = 
functions.firestore.document('active_delivery/{distributorId}')
.onDelete(adt => {
    const ad = adt.data()
    const time: Date = (admin.firestore.Timestamp.now()).toDate()
    const doc_name = month[time.getMonth()]+'_'+time.getFullYear()

    const data ={
        allotted_delivery: admin.firestore.FieldValue.increment(ad.total),
        order_delivered: admin.firestore.FieldValue.increment(ad.delivered+1),
    }

    return admin.firestore().doc(`user/${adt.id}/stats/${doc_name}`)
    .set(data, {merge:true}).catch((e) => {console.log(e)})
})
