from pathlib import Path
import zipfile, re, sys
root=Path('iconpack/src/main')
art=Path('iconpack/GameOS_Comic_Icons_PNG.zip')
if not art.exists():
    print('Missing iconpack/GameOS_Comic_Icons_PNG.zip. Upload the artwork ZIP before building.')
    sys.exit(1)
drawable=root/'res'/'drawable-nodpi'
drawable.mkdir(parents=True,exist_ok=True)
mappings={
 'youtube':'com.google.android.youtube',
 'play_store':'com.android.vending',
 'gmail':'com.google.android.gm',
 'photos':'com.google.android.apps.photos',
 'amazon':'com.amazon.mShop.android.shopping',
 'phone':'com.samsung.android.dialer',
 'messages':'com.google.android.apps.messaging',
 'opera':'com.opera.browser',
 'camera':'com.sec.android.app.camera',
 'chrome':'com.android.chrome',
 'settings':'com.android.settings',
 'clock':'com.sec.android.app.clockpackage',
 'calendar':'com.samsung.android.calendar',
 'contacts':'com.samsung.android.app.contacts',
 'gallery':'com.sec.android.gallery3d',
 'my_files':'com.sec.android.app.myfiles',
 'maps':'com.google.android.apps.maps',
 'drive':'com.google.android.apps.docs',
 'youtube_music':'com.google.android.apps.youtube.music',
 'spotify':'com.spotify.music',
 'netflix':'com.netflix.mediaclient',
 'disney_plus':'com.disney.disneyplus',
 'prime_video':'com.amazon.avod.thirdpartyclient',
 'internet':'com.sec.android.app.sbrowser',
 'notes':'com.samsung.android.app.notes',
 'calculator':'com.sec.android.app.popupcalculator',
 'health':'com.sec.android.app.shealth',
 'twitch':'tv.twitch.android.app',
 'reddit':'com.reddit.frontpage',
 'discord':'com.discord',
 'facebook':'com.facebook.katana',
 'instagram':'com.instagram.android',
 'messenger':'com.facebook.orca',
 'x':'com.twitter.android',
 'tiktok':'com.zhiliaoapp.musically',
 'snapchat':'com.snapchat.android',
 'galaxy_store':'com.sec.android.app.samsungapps',
}
with zipfile.ZipFile(art) as z:
    for name in z.namelist():
        stem=Path(name).stem.lower()
        if re.fullmatch(r'[a-z][a-z0-9_]*',stem) and name.lower().endswith('.png'):
            (drawable/(stem+'.png')).write_bytes(z.read(name))
existing={p.stem for p in drawable.glob('*.png')}
if not existing: sys.exit('No icons found in archive')
xml=root/'res'/'xml'
xml.mkdir(parents=True,exist_ok=True)
items=['<?xml version="1.0" encoding="utf-8"?>','<resources>']
for name,package in mappings.items():
    if name in existing:
        items.append(f'  <item component="ComponentInfo{{{package}/{package}.MainActivity}}" drawable="{name}"/>')
items.append('</resources>')
(xml/'appfilter.xml').write_text('\n'.join(items))
# Android icon packs typically expose appfilter.xml as a raw resource.
raw=root/'res'/'raw'
raw.mkdir(parents=True,exist_ok=True)
(raw/'appfilter.xml').write_text('\n'.join(items))
print(f'Prepared {len(existing)} icons')
