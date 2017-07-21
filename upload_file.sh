

TOKEN=$( echo "{\"key\": \"36b75fa39f3c0aac17080f2f05c8721f4a5c2d30\"}" | jq -r '.key' );
REPO_ID=$( curl -k -L --silent --header "Authorization: Token ${TOKEN}" https://files.n-core.ru/api2/repos/ | jq -r ".[] | select (.name==\"gitlab\") | .id" )
UPLOAD_HOST=$( echo "https://files.n-core.ru/api2/repos/${REPO_ID}/upload-link/" )
UPLOAD_LINK=$( curl -k -L --silent --header "Authorization: Token ${TOKEN}" "${UPLOAD_HOST}" | jq --raw-output '.')
PWD=$(echo "app/build/outputs/apk")
FILENAME=$(ls $PWD | head -n 1)
FILEPATH=$(echo "${PWD}/${FILENAME}")
DATETIME=$(date +"%D %T ")

curl -k -L --silent --header "Authorization: Token ${TOKEN}" -F "file=@${FILEPATH}" -F "filename=${DATETIME}${FILENAME}" -F parent_dir=/  "${UPLOAD_LINK}"

