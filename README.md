Stay Anonymous
==========================================

## Motication:  
 The right to express our true opinions 
- Let people express their true thoughts while remaining ANONYMOUS
- Let people find their partners base on their shared interests instead of IDENTITY

## Use Case
- In class, student wants to give instant feedback to whole class, but she doesn’t want to say it publicly
- Classmates having critical reviews of others’ work.
- - Making critical decision in company, however employees don’t want to say their real thoughts in price of exposing their identities. Because they are afraid of being blamed..

## Functions
- Anonymous Chat
- Anonymous Vote
- Partner Finding

## Challenge
1. Comparatively new technology : Google mobile backend starter.
2. Need to perform hundreds of iteration to perform a single task with lack of documentation
3. To understand the back-end side of codes was also a time taking process.
5. Fetching records is also a big challenge : Continuous query.
6. UI/UX design, UI fluency 

## Implementation(1): Partner Finding
- Store every “event data” through GCM Service
- Implement “Continuous” Query 
- Send “Latest” Query Results to every users

## Implementation(2): Anonymous Chat / Vote
- Use google app engine & GCM services for both vote and chat
- Future_Past query of app engine
- GCM to achieve N : N online chatting

## Products
- [App Engine][1]
- [Android][2]

## Language
- [Java][3]

## APIs
- [Google Cloud Endpoints][4]

## Setup Instructions
The instruction below lists just some key steps.
For detailed setup instructions and documentation visit [Google App Engine developer site] (https://developers.google.com/cloud/samples/mbs).

1. Make sure you have Android SDK with Google APIs level 15 or above installed.

2. Import the project into Eclipse.

3. Make sure you have Google APIs selected in your project properties. This option is under Android in Project Build Target.

4. Update the value of `PROJECT_ID` in
   `src/com/google/cloud/backend/android/Consts.java` to the app_id of your
   deployed Mobile Backend [5]. Make sure that your Mobile Backend is configured
   with OPEN mode. Also update your `PROJECT_NUMBER` and `WEB_CLIENT_ID` with the values from your console project.

5. Update the value of `DEFAULT_ROOT_URL` in
   `endpoint-libs/libmobilebackend-v1/mobilebackend/mobilebackend-v1-generated-source/Mobilebackend.java` to your own project.

6. Run the application.

## Future Work
- More stable
- SMS availability
- Multimedia : videos or pictures


[1]: https://developers.google.com/appengine
[2]: http://developer.android.com/index.html
[3]: http://java.com/en/
[4]: https://developers.google.com/appengine/docs/java/endpoints/
[5]: https://github.com/GoogleCloudPlatform/solutions-mobile-backend-starter-java

