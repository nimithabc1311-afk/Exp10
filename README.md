# Experiment 10: Implement Data Sharing Using Contacts Content Provider

| **Name** | Nimitha BC |
| **USN** | 25MCAR0132 |
| **Course** | MCA |
| **Platform** | Android Studio, Kotlin |
| **Experiment** | 7 |

## 1. About the Experiment

Android apps run in separate sandboxes, so one app cannot directly open another app's data. When data has to be shared safely between apps, Android uses **Content Providers**. This experiment builds an app that shares data with the phone's built-in Contacts app through the **Contacts Content Provider**. The app can:

- **Read** the contacts stored on the device and show them in a list
- **Search** contacts by name
- **Add** a new contact that then appears in the phone's Contacts app

---

## 2. Concept / Technology Behind It

### Content Provider
A Content Provider is an Android component that manages a shared set of app data and exposes it to other apps in a controlled way. The data is identified by a **content URI**, and access is protected by **permissions**.

### Key classes used

| Class | Role in this app |
|-------|------------------|
| `ContactsContract` | The contract class that defines the URIs and column names of the Contacts provider |
| `ContentResolver` | The client that sends requests to a provider. `contentResolver.query()` reads data and `applyBatch()` writes data |
| `Cursor` | Holds the rows returned by a query so they can be read one by one |
| `ContentProviderOperation` | Describes one insert operation; several are sent together as a batch |
| `ContactsContract.CommonDataKinds.Phone` | Gives the phone-number table, with `DISPLAY_NAME` and `NUMBER` columns |

### Permissions
Contacts are private user data, so the app needs two permissions:
- `READ_CONTACTS` to read contacts
- `WRITE_CONTACTS` to add contacts

Both are declared in `AndroidManifest.xml` and also requested from the user **at runtime** (Android 6.0 and above).

---

## 3. Scenario Used to Demonstrate It

A **Contact Manager** app:

1. On launch, the app asks for permission to access contacts.
2. Once permission is granted, all contacts on the device are listed with their phone numbers, along with a count of how many were found.
3. The user types in the search box to filter contacts by name. The list updates as they type.
4. The user enters a name and phone number and taps **Add Contact**. The new contact is saved through the Contacts provider and appears in the list straight away. It is also visible in the phone's own Contacts app, which proves the data is shared between apps.

The screen also shows the student name and USN at the top.

---

## 4. Project Folder and File Structure

```
Experiment10/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── java/com/example/experiment10/
│               └── MainActivity.kt
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── screenshots/
│   ├── output.png
│   ├── test1_permission_list.png
│   ├── test2_add_contact.png
│   └── test3_search.png
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Builds the screen in code (no XML layout needed), requests runtime permissions, reads and searches contacts with `ContentResolver.query()`, and adds contacts with `ContentResolver.applyBatch()` |
| `AndroidManifest.xml` | Declares `MainActivity` as the launcher activity and the `READ_CONTACTS` and `WRITE_CONTACTS` permissions |
| `build.gradle.kts` | Gradle configuration and dependencies |
| `screenshots/` | Output and test case screenshots shown in this README |

---

## 5. Output

<img width="1533" height="862" alt="WhatsApp Image 2026-10-23 at 6 56 55 PM" src="https://github.com/user-attachments/assets/9689480d-f1f8-4f60-ab11-8cbac5c7c614" />


---

## 6. Test Cases

### Test Case 1: Request permission and list contacts

| | |
|---|---|
| **Input** | Launch the app and tap **Allow** on the contacts permission dialog |
| **Expected result** | Toast "Permission granted", and the contacts on the device are listed with their numbers and a "Contacts found" count. The header shows Name: Nimitha BC and USN: 25MCAR0132 |
| **Status** | Pass |

**Screenshot:**

<img width="1533" height="862" alt="WhatsApp Image 2026-10-23 at 6 56 55 PM" src="https://github.com/user-attachments/assets/da51ea0e-e7dd-4e59-9bda-43d5c5f7847f" />


### Test Case 2: Add a new contact through the Content Provider

| | |
|---|---|
| **Input** | Name: `Nimitha BC`, Number: your phone number, then tap **Add Contact** |
| **Expected result** | Toast "Contact added", and the new contact appears in the list. It is also visible in the phone's Contacts app |
| **Status** | Pass |

**Screenshot:**

<img width="1535" height="862" alt="WhatsApp Image 2026-10-23 at 6 56 55 PM (1)" src="https://github.com/user-attachments/assets/7ae523ad-c95c-49a3-ba70-b78294f13da8" />




## 7. How to Run

1. Open the project in Android Studio and wait for Gradle sync to finish.
2. Select an emulator or connect a physical device.
3. Click the green **Run** button.
4. Tap **Allow** when the app asks for contacts permission.

---

## 8. Conclusion

The experiment shows how a Content Provider lets one app share data with another in a controlled, permission-protected way. 
The app read, searched and added contacts in the phone's Contacts database without accessing its storage directly, using only `ContentResolver` and the `ContactsContract` URIs.
