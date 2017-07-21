

TOKEN=$( echo "{\"key\": \"36b75fa39f3c0aac17080f2f05c8721f4a5c2d30\"}" | jq -r '.key' );
REPO_ID=$( curl -k -L --silent --header "Authorization: Token ${TOKEN}" https://files.n-core.ru/api2/repos/ | jq -r ".[] | select (.name==\"gitlab\") | .id" )
UPLOAD_HOST=$( echo "https://files.n-core.ru/api2/repos/${REPO_ID}/upload-link/" )
UPLOAD_LINK=$( curl -k -L --silent --header "Authorization: Token ${TOKEN}" "${UPLOAD_HOST}" | jq --raw-output '.')
FILENAME=$( echo "${CI_COMMIT_TAG}_${CI_RUNNER_ID}_${CI_COMMIT_REF_NAME}.apk" )
PWD=$(pwd)

echo "TOKEN: " + $TOKEN
echo "REPO_ID: " + $REPO_ID
echo "UPLOAD_HOST: " + $UPLOAD_HOST
echo "UPLOAD_LINK: " + $UPLOAD_LINK
echo "FILENAME: " + $FILENAME
echo "PWD: " + $PWD


curl -k -L --silent --header "Authorization: Token ${TOKEN}" -F "file=@/home/sapotero/AndroidStudioProjects/RxTest/${FILENAME}" -F "filename=${FILENAME}" -F parent_dir=/  "${UPLOAD_LINK}"

