# EduID Android

The main android native mobile app for EduID-Mobile project.
This will act as an authorization provider, and provides authorization service(EduID) to the android user.

EduID App communicates directly to the Trust Agent(OIDC Provider) mostly for the authentication and also the resource provider for the authorization process.

This app supports the NAIL API that enables the app to exchange authorization data with third party mobile apps.
In a usual case third party apps that want to access registered service(in OIDC provide) no need to implement the authentication and authorization function anymore. It just need to implement NAIL API, that triggers the EduID App and at the end will return the required credentials/token for accessing the service(ex. Moodle Service).
More detail explanation about NAIL API could be found [here](https://github.com/EduID-Mobile/Architecture-and-Requirements/blob/master/40-nail-api.md).
