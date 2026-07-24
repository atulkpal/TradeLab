# Implementation Plan: Hosting TradeLab Website

This plan covers the deployment of the TradeLab website to Firebase Hosting.

## User Review Required

> [!NOTE]
> The website will be deployed to the Firebase project **tradelab-4f858**.
> I will also enable `cleanUrls` in the configuration so that you can access pages like `/privacy` instead of `/privacy.html`.

## Proposed Changes

### [Firebase Configuration]

#### [MODIFY] [firebase.json](file:///C:/Users/Atul/AndroidStudioProjects/TradeLab/firebase.json)
- Add `"cleanUrls": true` to the hosting configuration.
- Keep the catch-all rewrite for the main application tabs, but ensure it doesn't interfere with static pages.

### [Deployment]

- Execute `npx -y firebase-tools@latest deploy --only hosting` to push the site live.

## Verification Plan

### Manual Verification
- Once deployed, visit the following URLs to ensure they resolve correctly:
    - [Home](https://tradelab-4f858.web.app/)
    - [Privacy Policy](https://tradelab-4f858.web.app/privacy)
    - [Terms of Service](https://tradelab-4f858.web.app/terms)
    - [Delete Account](https://tradelab-4f858.web.app/delete-account)
