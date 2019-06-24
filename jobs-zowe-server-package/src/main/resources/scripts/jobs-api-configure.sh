# Add static definition for jobs-api
cat <<EOF >${STATIC_DEF_CONFIG_DIR}/jobs-api.ebcidic.yml
#
services:
  - serviceId: jobs
    title: IBM z/OS Jobs
    description: IBM z/OS Jobs REST API service
    catalogUiTileId: jobs
    instanceBaseUrls:
      - https://${ZOWE_EXPLORER_HOST}:${JOBS_API_PORT}/
    homePageRelativeUrl:
    routedServices:
      - gatewayUrl: api/v1
        serviceRelativeUrl: api/v1/jobs
    apiInfo:
      - apiId: com.ibm.jobs
        gatewayUrl: api/v1
        version: 1.0.0
        documentationUrl: https://${ZOWE_EXPLORER_HOST}:${JOBS_API_PORT}/swagger-ui.html
catalogUiTiles:
  jobs:
    title: z/OS Jobs services
    description: IBM z/OS Jobs REST services
EOF
iconv -f IBM-1047 -t IBM-850 ${STATIC_DEF_CONFIG_DIR}/jobs-api.ebcidic.yml > $STATIC_DEF_CONFIG_DIR/jobs-api.yml
rm ${STATIC_DEF_CONFIG_DIR}/jobs-api.ebcidic.yml
chmod 755 $STATIC_DEF_CONFIG_DIR/jobs-api.yml