# JFrog Artifactory Report REST API
Find the top most popular jar file (artifact) in a maven repository. 

## Routes {GET}
 - Reqeust :
   <server>/reports/artifacts/:count  - returns top most artifact downloads from repository 
 - Response: Returns array of most popular jar (binary) from repo

```
{
    "results": [
        {
            "uri": "http://<server url>/artifactory/jcenter-cache/asm/asm-tree/3.3/asm-tree-3.3.jar",
            "downloadCount": 16,
            "lastDownloaded": 1575412952029,
            "lastDownloadedBy": "anonymous",
            "remoteDownloadCount": 0,
            "remoteLastDownloaded": 0
        },
        {
            "uri": "http://<server url>/artifactory/jcenter-cache/commons-fileupload/commons-fileupload/1.2.2/commons-fileupload-1.2.2.jar",
            "downloadCount": 16,
            "lastDownloaded": 1575412951715,
            "lastDownloadedBy": "anonymous",
            "remoteDownloadCount": 0,
            "remoteLastDownloaded": 0
        }
    ]
}
```

## Environment variables to customize the service 
    - END_POINT  -- define the jfrog server info, e.g. http://<servername>/artifactory/api
    - JFROG_REPO -- jfrog repository name e.g. jcenter-cache
    - JFROG_SEARCH -- jfrog search api end point e.g. /search/aql
    - USERNAME -- jfrog user name, its configure for basic authentication
    - PASSWORD -- jfrog password, its configure for basic authentication


## Running using Docker
    - download the code 
        git clone 
    - docker build -t jfrog-app .
    - docker run -p 8080:8080 -d jfrog-app:latest
       - optionally, you can run using above variable for other settings.    
