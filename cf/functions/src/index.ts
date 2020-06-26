import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
//import * as nodemailer from 'nodemailer';
//import * as cors from 'cors';
admin.initializeApp()
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
            message += ""+req.query.extra
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
            const element = docs[i];
            if(element.data().active!=null){
                isAnyActive = true
                active_sub = element.data()
                break
            }
        }
        if(!isAnyActive){
            return res.send({status: 202,msg: 'No Active Delivery',data: null})
        }
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
                let promise: any
                if(newDt.pending==0){
                    isDeliveryPending = false
                    promise = adt.ref.delete()
                }else{
                    promise = adt.ref.update(newDt)
                }

                return promise
                .then(()=>{
                    return admin.firestore().doc(`user/${to}/subscriptions/${active_sub?.sId}`).update('active', null)
                    .then(()=>{
                        return res.send({status: isDeliveryPending?200:204,msg: 'Order Delivered',data: adt.id})
                    }).catch((e) => {return res.send({status: 404,msg: 'Error Setting Active To Null!!',data: e})})
                }).catch((e) => {return res.send({status: 404,msg: 'Error Updating Active Delivery!!',data: e})})
            }) .catch((e) => {return res.send({status: 404,msg: 'Error Creating Order!!',data: e})})
        }).catch((e) => {return res.send({status: 404,msg: 'Delivery Not Found!!',data: e})})
    }).catch((e) => {return res.send({status: 404,msg: 'Invalid User!!',data: e})})
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
    const month = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 
'NOV', 'DEC']

    const order = dt.data()
    const time: Date = (<admin.firestore.Timestamp>order.time).toDate()
    
    const doc_name = month[time.getMonth()]+'_'+time.getFullYear()

    return admin.firestore().doc('admin/meta-data').get()
    .then(snap => {
        const rate: number = snap.data()?.rate

        const data = {
            due: admin.firestore.FieldValue.increment(rate*order.amount),
            orders: admin.firestore.FieldValue.arrayUnion(dt.ref),
            consumption: admin.firestore.FieldValue.increment(order.amount)
        }

        return admin.firestore().doc(`user/${order.to}/stats/${doc_name}`)
        .set(data, {merge: true})

    }).catch((e) => {console.log(e)})
})
