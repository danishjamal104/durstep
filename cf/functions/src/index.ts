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