# Audio-Transcription-Service

Steps to run and test the service:

1. Start the remote ATS sample service by checking the instructions here - https://github.com/schwers/asr-sample-service/blob/main/README.md
2. To run the Audio-Trancription-Service, follow the instructions below:
   Clone the current repository locally.
   Run this command:  ./gradlew clean build
   Run this command: ./gradlew bootRun --info

This will start the Spring boot service on localhost at port 8081. It can be configured in application.properties file in the project. The job metadata with contents and status is persisted in disk files created under "/tmp/ats".

Try cURL calls(all calls should work):

1. Transcribe : curl -X POST 'http://localhost:8081/api/transcribe' \                                          
     -d 'audioPaths=audio-file-6.wav&audioPaths=audio-file-8.wav&userId=ambience-1'

2. GetTranscript : curl -X GET 'http://localhost:8081/api/transcript/2'   here 2 is jobId. Make sure this is a previously submitted/created jobId

3. Search Transcript : curl -X GET 'http://localhost:8081/api/transcript/search?jobStatus=COMPLETED&userId=ambience-1'. Try this after running curl call 1. a few times to generate a few jobs for a given user. May have some errors as it replies on a disk file created under /tmp/ats/{userId}/job-status.txt

   

