# Document

## JitPack
> Step 1. Add the JitPack repository to your build file
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
> Step 2. Add the dependency
```gradle
dependencies {
	        implementation 'com.github.hachilib:admob_template:Tag'
	}
```

## Admob
> Confix manifest
```gradle
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```
```gradle
 <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />
```
> Dependencies google ads
```gradle
implementation ("com.google.android.gms:play-services-ads:22.4.0")
```
