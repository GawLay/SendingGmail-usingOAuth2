# SendingGmail-usingOAuth2
dependencies {  
    implementation fileTree(dir: 'libs', include: ['*.jar'])  
    implementation 'com.android.support:appcompat-v7:25.0.1'  
    implementation 'com.google.android.gms:play-services-auth:11.8.0'  
    implementation('com.google.api-client:google-api-client-android:1.23.0') {  
        exclude group: 'org.apache.httpcomponents'  
    }  
    implementation('com.google.apis:google-api-services-gmail:v1-rev82-1.23.0') {  
        exclude group: 'org.apache.httpcomponents'  
    }  
    implementation 'com.google.android.gms:play-services-auth:17.0.0'
    implementation 'com.google.android.gms:play-services-basement:17.1.1'
    implementation 'com.google.android.gms:play-services-base:17.1.0'
    implementation files('libs/mail.jar')  
    implementation files('libs/activation.jar')  
    implementation files('libs/additionnal.jar')  
}  

download required jar files from this link http://www.mediafire.com/file/6lo99cjmk7y52o9/11.JavaMailApiJars.zip/file

You need to set your package name and SHA KEY to Google API CONSOLE
