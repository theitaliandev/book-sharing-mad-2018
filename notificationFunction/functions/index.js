'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/notifications/{user_id}/{notification_id}').onWrite((change, context) => {

   const user_id = context.params.user_id;
   const notification_id = context.params.notification_id;

   console.log('We have a notification to send to : ', context.params.user_id);


   const deviceToken = admin.database().ref('/users/'+user_id+'/tokenId').once('value');
   return deviceToken.then(result => {

    const token_id = result.val();

    const payload = {
     notification: {
      title: "Book Request",
      body: "You've received a new Book Request",
      icon: "default"
     }
    };
    return admin.messaging().sendToDevice(token_id,payload).then(response => {
     console.log('This was the notification feature');
     console.log(token_id);
     return true;
    });
   });
 });
