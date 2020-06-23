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
    const message = ""+req.query.msg

    admin.firestore().doc(`user/${to}`).get()
    .then(ud => {
        const push_token = ud.data()?.push_token

        if(push_token===null){
            return res.send("No token found");
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

export const onNewDeliveryCreated = 
functions.firestore.document('active_delivery/{distributorId}')
.onCreate(dt => {
    const ad = dt.data()

    const ref: admin.firestore.DocumentReference = admin.firestore().doc(`active_delivery/${dt.id}`)

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

export const exceptNewDelivery = 
functions.firestore.document('delivery/{dId}')
.onCreate(dt => {
    const delivery = dt.data()

    const order = {
        to: delivery?.to,
        from: admin.firestore().doc(`user/${delivery?.active.id}`),
        amount: delivery?.amount,
        time: delivery?.time
    }

    return admin.firestore().collection(`orders/${dt.id}`).add(order)
    .then(() => {
        const promises:any = []

        promises.push(delivery?.subRef.update('active', null))
        promises.push(admin.firestore().doc(`delivery/${dt.id}`).delete())

        return Promise.all(promises)
        .then(() => {
            return delivery?.active.update({
                completed: admin.firestore.FieldValue.increment(1),
                pending: admin.firestore.FieldValue.increment(-1),
                delivered_list: admin.firestore.FieldValue.arrayUnion(delivery?.subRef)
            })
        }).catch(error => console.log(error))
    }).catch(error => console.log(error))
})