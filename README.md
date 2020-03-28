# OkLog 
Network logging interceptor for OkHttp. 
Logs an URL link with encoded network call data for every OkHttp call.

[ ![Download](https://api.bintray.com/packages/kaustav07/com.github.kaustav07/oklog3/images/download.svg?version=2.9.0) ](https://bintray.com/kaustav07/com.github.kaustav07/oklog3/2.9.0/link)

## About the Library
This is actually a extention library of OKLog library created by simonpercic

you can find it in [here](https://github.com/simonpercic/OkLog)

The original library had some limitations like below - 

- when request URL too long it comes in two lines in android studio for its 4000 char limitation, then we have to manually copy paste the remainingpart
- when the uri is too much long for large responses then even if you manually paste the whole url part in browser it gives a 414 Request URI too long error as 
there is an limitation of 8kb in request uri in http and also for hosting its server side component we use free heroku app so changing it to a larger values wasn't an option

Solutions - 

- For the first problem I used firebase dynamic links to shorten it.
- For the second problem I have used firebase realtime database to store the data and attach the unique key of the data to the url and I have also hosted the server side app myself so I fetch it there

## Usage

Now I have did a revamped of the server side app also to make the UI better ,  I have also put a PR for the same in the original app repo

its available in JCenter, you just have to replace the original import of the library with below -

if you want to use my version of the library then  first add `maven { url  "https://dl.bintray.com/kaustav07/com.github.kaustav07"}` under `allprojects -> repositories` in your project level gradle file like below - 



```groovy

    allprojects {
        repositories {
            maven {
                    url  "https://dl.bintray.com/kaustav07/com.github.kaustav07"

            }
        }
    }
    
```

then you just have to replace the original import of the library with below -

 ```groovy
 
     implementation 'com.github.kaustav07:oklog3:$latest_version'
     
 ```
 
its also availavle in the JCenter but if you want to use it from JCenter the urls are different as there cannot be two library with same name in JCenter,

your projects level gradle will be like below - 

```groovy

    allprojects {
        repositories {
            maven {
            		url  "https://jcenter.bintray.com"
            }
        }
    }
    
```

and your app level gradle implementation will be like below - 

 ```groovy
 
     implementation 'com.github.kaustav07:oklog3-kaustav07:$latest_version'
     
 ```

```java
//set the base url to point by url when you are setting up the interceptor

setBaseUrl("https://responseviewer.herokuapp.com/")

```

if you want to use the URL shortner you have create your firebase project and obtain a Web API key which is  located under project settings,
Then you have to implement the `GooleAuthTokenProvider` Interface and return the key and baseurl for your dynamic links from there and use it like below while setting up the interceptor - 

```java
//set the implemented class

googleAuthProvider(new GoogleAccessProvide())

```

for the second solution there is no easy way because I have interface `URLShortenAPIKeyProvider` which you can implement and return the access token for accessing the realtime database, but i have no way to get that access token in my web app so if you want to you this contact me
Or I will put the repo in github in some days and you can for it and host in your own and contact me to know in which fields you have to change to access your firebase project from the web app


## License
Open source, distributed under the MIT License. See [LICENSE](LICENSE) for details.
