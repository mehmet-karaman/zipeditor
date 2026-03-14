#!/bin/bash
# Removes version directories from SourceForge that no longer have a corresponding git tag.
# Requires: lftp, git with fetched tags
# Environment: SFTP_USERNAME, SFTP_PASSWORD

set -e

REMOTE_HOST="web.sourceforge.net"
REMOTE_PATH="/home/project-web/zipeditor/htdocs/update"

# Collect expected versions from git tags
mapfile -t EXPECTED < <(git tag -l 'ZipEditor_[0-9]*' \
  | sed 's/ZipEditor_//; s/_/./g' \
  | grep -E '^[0-9]+\.[0-9]+\.[0-9]+' \
  | sort -V)

if [ ${#EXPECTED[@]} -eq 0 ]; then
  echo "No version tags found, skipping cleanup"
  exit 0
fi

echo "Expected versions from tags: ${EXPECTED[*]}"

# lftp needs to accept unknown host keys in CI
export LFTP_SETTINGS="set sftp:auto-confirm yes; set ssl:verify-certificate no;"

# List remote directories via lftp
echo "Listing remote directories..."
REMOTE_LISTING=$(lftp -u "${SFTP_USERNAME},${SFTP_PASSWORD}" "sftp://${REMOTE_HOST}" -e "${LFTP_SETTINGS} cls ${REMOTE_PATH}/; quit")

echo "Remote listing: ${REMOTE_LISTING}"

STALE=()
while IFS= read -r entry; do
  dir=$(basename "$entry")
  # Only consider version-like directories (e.g. 1.2.0, 1.2.0.rc1)
  [[ "$dir" =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]] || continue

  found=false
  for exp in "${EXPECTED[@]}"; do
    if [ "$dir" = "$exp" ]; then
      found=true
      break
    fi
  done

  if [ "$found" = false ]; then
    STALE+=("$dir")
  fi
done <<< "$REMOTE_LISTING"

if [ ${#STALE[@]} -eq 0 ]; then
  echo "No stale versions to remove"
  exit 0
fi

echo "Removing stale versions: ${STALE[*]}"

# Build lftp command to remove all stale directories
LFTP_CMDS="${LFTP_SETTINGS} "
for dir in "${STALE[@]}"; do
  LFTP_CMDS="${LFTP_CMDS}rm -rf ${REMOTE_PATH}/${dir}; "
done

lftp -u "${SFTP_USERNAME},${SFTP_PASSWORD}" "sftp://${REMOTE_HOST}" -e "${LFTP_CMDS} quit"

echo "Cleanup complete"
