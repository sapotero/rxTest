PWD=$(echo "app/build/outputs/apk")
FILENAME=$(ls $PWD | head -n 1)
FILEPATH=$(echo "${PWD}/${FILENAME}")

adb push "${FILEPATH}" /data/local/tmp/sapotero.rxtest.test
adb shell pm install -r "/data/local/tmp/sapotero.rxtest"
adb shell am instrument -w -r   -e debug false -e class sapotero.rxtest.views.activities.SettingsActivityTest sapotero.rxtest.test/