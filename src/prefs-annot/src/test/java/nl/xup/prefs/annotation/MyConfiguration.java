package nl.xup.prefs.annotation;

import java.lang.reflect.Field;

@Preferences(root = Root.SYSTEM, node = "/path/to/the/node")
public class MyConfiguration {

	@Preference
	private String mySetting;

	@Preference(
			node = "/some/other/node", 
			name = "settingName", 
			notificationMethod = "otherNotificationMethod"
	)
	private String mySetting2;

	@PreferenceNotification
	public void notificationMethod(Field field, Object oldValue, Object newValue) {
		// Process notification.
	}

	public void otherNotificationMethod(Field field, Object oldValue,
			Object newValue) {
		// Process notification.
	}

	public String getMySetting() {
		return mySetting;
	}

	public void setMySetting(String mySetting) {
		this.mySetting = mySetting;
	}

	public String getMySetting2() {
		return mySetting2;
	}

	public void setMySetting2(String mySetting2) {
		this.mySetting2 = mySetting2;
	}
}
