# E-Partogram-TWA
___

## **Description**
* The App is built using trusted web activity which renders the content of [web-client](https://epartogram.captainirs.dev)


* Trusted Web Activity (TWA) is a technology that allows a native app to display a trusted web page in a full-screen mode. Essentially, it is a way to integrate a website or web app into a native mobile app using the power of the Android WebView component.

    |<img src=https://imgur.com/VxofFUo.png width="100%" />   | <img src=https://imgur.com/OPKn35p.png width="100%" />   |
    |---|---|


## **APK**
* Download the [Release-APK](./app/release/app-release.apk)

## **Tech-Stack**

|<img src="https://upload.wikimedia.org/wikipedia/commons/thumb/3/3e/Android_logo_2019.png/800px-Android_logo_2019.png" width="100%" height="220"/>  | <img src="https://logowik.com/content/uploads/images/kotlin.jpg" width="100%" height="220"/>   |
|---|---|
|<img src="https://logowik.com/content/uploads/images/android-studio8113.jpg" width="100%" height="220"/>  | <img src="https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQnkV5YQc8z_XGd_rF56tBqkgbJ4ZJt57LLbq-cedSMOWcskrB8KRv-Aw9DljruX01Aszo&usqp=CAU" width="100%" height="220"/>   |


## **Features**
*Firebase Cloud Messaging(FCM) Android Push Notification*

* The Application Leverages the `Firebase Cloud Messaging(FCM)` service's native android push notification instead of web-push to deliver an authentic native experience to end users.
    - Native android captures the FCM token
    - Attaches it to the launch URL as query param( named `_notifyToken`) and launches the TWA with this url.
    - The [web-client](https://epartogram.captainirs.dev) captures the query params and sends it to the backend once the user logins
       
  
        ![](https://imgur.com/BbVH7Jd.png)

        ___
*Offline First TWA*
* The Application Leverages the native capability of Android to handle Offline Capabilities. While Launching the TWA for the first time the app checks two things
    
    - Using Android's NetworkCapabilities Library it checks whether the user is offline and navigates to fallback screen which prompts the user that `"You Are Offline"` with a ***Retry*** Button.

        ![](https://imgur.com/ekzr0Gp.png)

        ___    

    - Checks if the [web-client](https://epartogram.captainirs.dev) is down and if so it navigates the user to fallback screen which prompts the user that `"Server Under Maintance"` with a ***Retry*** Button.

        ![](https://imgur.com/zF0v47B.png)
        ___   
## **Requirements**
* [Android Studio](https://developer.android.com/studio)

## **Setup**

* Clone the Repo

    ```bash
    git clone <REPO_LINK>
    ```

* Open the Repository in Android Studio

* Add google-services.json file (app/google-services.json)

* Build and Run the App



