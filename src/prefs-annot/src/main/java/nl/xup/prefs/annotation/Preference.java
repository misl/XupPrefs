package nl.xup.prefs.annotation;

public @interface Preference {
	public String name() default "";
	public String node() default "";
	public String notificationMethod() default "";
}
