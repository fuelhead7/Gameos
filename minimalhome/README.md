# Minimal Home
A standalone, minimalist Samsung/Android launcher. This does not replace the Samsung One UI system theme.

## Design
- Near-black background and white, quiet typography
- Date and clock, with no decorative banners
- Tap **Apps** to search all installed launchable apps
- Long-press an app in the drawer to add/remove it from favorites
- Favorites persist locally in SharedPreferences
- Uses each installed app's real icon so matching never requires manually assigning mappings

## Install
GitHub Actions > **Build Minimal Home APK** > download the artifact and install. Android Settings > Apps > Choose default apps > Home app > Minimal Home.
To return to One UI Home, change the default Home app in Settings.

## Future
Custom accent colors, adjustable density, widget host, wallpaper picker, backup/export, and optional monochrome icon masking. Nothing here needs Accessibility permissions.
