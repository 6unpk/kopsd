enum class PSDImageResourceId(val value: Int, val endValue: Int? = null) {
    CHANEL_COUNT(0x03E8),
    MACOS_PRINT(0x03E9),
    MACOS_PAGE_FORMAT(0x03EA),
    INDEXED_COLOR_TABLE(0x03EB),
    RESOULUTION_INFO_STRUCTURE(0x03ED),
    NAME_OF_ALPHA_CHANNEL_SERIES(0x03EE),
    _1077DISPLAY_INFO(0x03EF),
    CAPTION(0x03F0),
    BORDER_INFOMATION(0x03F1),
    BACKGROUND_COLOR(0x03F2),
    PRINT_FLAGS(0x03F3),
    GRAY_SCALE(0x03F4),
    COLOR_HALFTONING(0x03F5),
    DUOTONE_HALFTONING(0x03F6),
    GRAYSCALE_AND_MULTICHANNEL_TRANSFER_FUNCTION(0x03F7),
    COLOR_TRANSFER_FUNCTIONS(0x03F8),
    DUOTONE_TRANSFER_FUNCTIONS(0x03F9),
    DUOTONE_IMAGE_INFORMATION(0x03FA),
    TWO_BYTE_BLACK_WHITE_DOT_RANGE(0x03FB),
    OBSOLETE_1(0x03FC),
    EPS_OPTIONS(0x03FD),
    QUICK_MASK_INFORMATION(0x03FE),
    OBSOLETE_2(0x03FF),
    LAYER_STATE_INFORMATION(0x0400),
    WORKING_PATH(0x0401),
    LAYER_GROUPS_INFORMATION(0x0402),
    OBSOLETE_3(0x0403),
    IPTC_NAA_RECORDS(0x0404),
    RAW_FORMAT_IMAGE_MODE(0x0405),
    JPEG_QUALITY(0x0406),
    GRID_AND_GUID_INFORMATION(0x0408),
    THUMBNAIL_RESOURCE_PHOTOSHOP_4_0(0x0409),
    COPYRIGHT_FLAG(0x040A),
    URL(0x040B),
    THUMBNAIL_RESOURCE_PHOTOSHOP_5_0(0x040C),
    GLOBAL_ANGLE(0x040D),
    COLOR_SAMPLERS_RESOURCE(0x040E),
    ICC_PROFILE(0x040F),
    WATERMARK(0x0410),
    ICC_UNTAGGED_PROFILE(0x0411),
    EFFECTIVE_VISIBLE(0x0412),
    SPOT_HALFTONE(0x0413),
    DOCUMENT_SPECIFIC_ID_NUMBER(0x0414),
    UNICODE_ALPHA_NAME(0x0415),
    INDEXED_COLOR_TABLE_COUNT(0x0416),
    TRANSPARENCY_INDEX(0x0417),
    GLOBAL_Altitude(0x0419),
    SLICES(0x041A),
    WORKFLOW_URL(0x041B),
    JUMP_TO_XPEP(0x041C),
    ALPHA_IDENTIFIER(0x041D),
    URL_LIST(0x041E),
    VERSION_INFO(0x0421),
    EXIF_DATA_1(0x0422),
    EXIF_DATA_3(0x0423),
    XMP_METADATA(0x0424),
    CAPTION_DIGEST(0x0425),
    PRINT_SCALE(0x0426),
    PIXEL_ASPECT_RATIO(0x0428),
    LAYER_COMPS(0x0429),
    ALTERNATE_DUOTONE_COLORS(0x042A),
    ALTERNATE_SPOT_COLORS(0x042B),
    LAYER_SELECTION_ID(0x042D),
    HDR_TONING_INFORMATION(0x042E),
    PRINT_INFO(0x042F),
    LAYER_GROUPS_ENABLED_ID(0x0430),
    COLOR_SAMPLER_RESOURCE(0x0431),
    MEASUREMENT_SCALE(0x0432),
    TIMELINE_INFORMATION(0x0433),
    SHEET_DISCLOSURE(0x0434),
    DISPLAYINFO_STRUCTURE_TO_SUPPORT_FLOATING_POINT_COLORS(0x0435),
    ONION_SKINS(0x0436),
    COUNT_INFORMATION(0x0438),
    PRINT_INFORMATION(0x043A),
    PRINT_STYLE(0x043B),
    MACOS_NSPRINTINFO(0x043C),
    WINDOWS_DEV_MODE(0x043D),
    AUTO_SAVE_FILE_PATH(0x043E),
    AUTO_SAVE_FILE_FORMAT(0x043F),
    PATH_SELECTION_STATE(0x0440),
    PATH_INFORMATION(0x07D0, 0x0BB6),
    NAME_OF_CLIPPING_PATH(0x0BB7),
    ORIGIN_PATH_INFO(0x0BB8),
    PLUGIN_RESOURCES(0x0FA0, 0x1387),
    IMAGE_READY_VARIABLES(0x1B58),
    IMAGE_READY_DATA_SETS(0x1B59),
    IMAGE_READY_DEFAULT_SELECTED_STATE(0x1B5A),
    IMAGE_READY_7_ROLLOVER_EXPANDED_STATE(0x1B5B),
    IMAGE_READY_ROLLOVER_EXPANDED_STATE(0x1B5C),
    IMAGE_READY_SAVE_LAYER_SETTINGS(0x1B5D),
    IMAGE_READY_VERSION(0x1B5E),
    LIGHTROOM_WORKFLOW(0x1F40),
    PRINT_FLAGS_INFORMATION(0x2710)
}