package com.nyxhub.nyx

import java.io.File

object NyxConstants {
    /*
     * Termux organization variables.
     */

    /**
     * Termux package name
     */
    // Default: "com.termux"
    private const val TERMUX_PACKAGE_NAME: String = "com.termux"

    /**
     * Termux app internal private app data directory path
     */
    private const val  // Default: "/data/data/com.termux"
        TERMUX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH: String = "/data/data/$TERMUX_PACKAGE_NAME"

    /**
     * Termux app Files directory path
     */
    // Default: "/data/data/com.termux/files"
    const val TERMUX_FILES_DIR_PATH: String = "$TERMUX_INTERNAL_PRIVATE_APP_DATA_DIR_PATH/files"

    /**
     * Termux app $PREFIX directory path
     */
    // Default: "/data/data/com.termux/files/usr"
    const val TERMUX_PREFIX_DIR_PATH: String = "$TERMUX_FILES_DIR_PATH/usr"

    /**
     * Termux app $PREFIX directory
     */
    val TERMUX_PREFIX_DIR: File by lazy { File(TERMUX_PREFIX_DIR_PATH) }

    /**
     * Termux app $PREFIX/bin directory path
     */
    // Default: "/data/data/com.termux/files/usr/bin"
    const val TERMUX_BIN_PREFIX_DIR_PATH: String = "$TERMUX_PREFIX_DIR_PATH/bin"

    /**
     * Termux app $PREFIX/tmp and $TMPDIR directory path
     */
    // Default: "/data/data/com.termux/files/usr/tmp"
    const val TERMUX_TMP_PREFIX_DIR_PATH: String = "$TERMUX_PREFIX_DIR_PATH/tmp"


    /**
     * Termux app usr-staging directory path
     */
    // Default: "/data/data/com.termux/files/usr-staging"
    const val TERMUX_STAGING_PREFIX_DIR_PATH: String = "$TERMUX_FILES_DIR_PATH/usr-staging"

    /**
     * Termux app usr-staging directory
     */
    val TERMUX_STAGING_PREFIX_DIR: File by lazy { File(TERMUX_STAGING_PREFIX_DIR_PATH) }

    // Default: "/data/data/com.termux/files/home/.termux/background/background_portrait.jpeg"
    /// public static final String TERMUX_BACKGROUND_IMAGE_PORTRAIT_PATH = TERMUX_BACKGROUND_DIR_PATH + "/background_portrait.jpeg";
    /**
     * Termux app $HOME directory path
     */
    // Default: "/data/data/com.termux/files/home"
    const val TERMUX_HOME_DIR_PATH: String = "$TERMUX_FILES_DIR_PATH/home"

    /**
     * Termux app storage home directory path
     */
    // Default: "/data/data/com.termux/files/home/storage"
    private const val TERMUX_STORAGE_HOME_DIR_PATH: String = "$TERMUX_HOME_DIR_PATH/storage"

    /**
     * Termux app storage home directory
     */
    val TERMUX_STORAGE_HOME_DIR: File by lazy { File(TERMUX_STORAGE_HOME_DIR_PATH) }

    /**
     * Termux and plugin apps directory path
     */
    // Default: "/data/data/com.termux/files/apps"
    const val TERMUX_APPS_DIR_PATH: String = "$TERMUX_FILES_DIR_PATH/apps"


    const val CONFIG_PATH: String = "$TERMUX_FILES_DIR_PATH/.termux"
}
