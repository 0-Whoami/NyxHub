package com.nyxhub.file

import android.content.Context
import com.nyxhub.file.filesystem.FileType
import com.nyxhub.file.filesystem.FileTypes
import com.termux.shared.termux.NyxConstants
import com.termux.shared.termux.NyxConstants.TERMUX_PREFIX_DIR_PATH
import java.io.File
import java.util.regex.Pattern


object FileUtils {
    /**
     * Required file permissions for the working directory for app usage. Working directory must have read and write permissions.
     * Execute permissions should be attempted to be set, but ignored if they are missing
     */
    // Default: "rwx"
    private const val APP_WORKING_DIRECTORY_PERMISSIONS: String = "rwx"

    /**
     * Checks whether a directory file exists at `filePath`.
     *
     * finding if file exists. Check [.getFileType]
     * for details.
     * @return Returns `true` if directory file exists, otherwise `false`.
     */

    private fun directoryFileExists(): Boolean {
        return getFileType(NyxConstants.TERMUX_FILES_DIR_PATH) != FileType.DIRECTORY
    }

    /**
     * Get the type of file that exists at `filePath`.
     *
     *
     * This function is a wrapper for
     * [FileTypes.getFileType]
     *
     * @param filePath    The `path` for file to check.
     * finding type. If set to `true`, then type of symlink target will
     * be returned if file at `filePath` is a symlink. If set to
     * `false`, then type of file at `filePath` itself will be
     * returned.
     * @return Returns the [FileType] of file.
     */
    private fun getFileType(filePath: String?): FileType {
        return FileTypes.getFileType(filePath, false)
    }

    /**
     * Validate the existence and permissions of directory file at path.
     *
     *
     * If the `parentDirPath` is not `null`, then creation of missing directory and
     * setting of missing permissions will only be done if `path` is under
     * `parentDirPath` or equals `parentDirPath`.
     *
     * @param filePath                            The `path` for file to validate or create. Symlinks will not be followed.
     * @param createDirectoryIfMissing            The `boolean` that decides if directory file
     * should be created if its missing.
     * @param permissionsToCheck                  The 3 character string that contains the "r", "w", "x" or "-" in-order.
     * @param setPermissions                      The `boolean` that decides if permissions are to be
     * automatically set defined by `permissionsToCheck`.
     * @param setMissingPermissionsOnly           The `boolean` that decides if only missing permissions
     * are to be set or if they should be overridden.
     * @return Returns the `error` if path is not a directory file, failed to create it,
     * or validating permissions failed, otherwise `null`.
     */

    private fun validateDirectoryFileExistenceAndPermissions(
        filePath: String?,
        createDirectoryIfMissing: Boolean,
        permissionsToCheck: String?,
        setPermissions: Boolean,
        setMissingPermissionsOnly: Boolean
    ): Boolean {
        if (filePath.isNullOrEmpty()) return false
        try {
            val file = File(filePath)
            var fileType = getFileType(filePath)
            // If file exists but not a directory file
            if (fileType != FileType.NO_EXIST && fileType != FileType.DIRECTORY) {
                return false
            }
            if (createDirectoryIfMissing || setPermissions) {
                // If there is not parentDirPath restriction or path is in parentDirPath
                // If createDirectoryIfMissing is enabled and no file exists at path, then create directory
                if (createDirectoryIfMissing && fileType == FileType.NO_EXIST) {
                    // Create directory and update fileType if successful, otherwise return with error
                    // It "might" be possible that mkdirs returns false even though directory was created
                    val result = file.mkdirs()
                    fileType = getFileType(filePath)
                    if (!result && fileType != FileType.DIRECTORY) return false
                }
                // If setPermissions is enabled and path is a directory
                if (setPermissions && permissionsToCheck != null && fileType == FileType.DIRECTORY) {
                    if (setMissingPermissionsOnly) setMissingFilePermissions(
                        filePath,
                        permissionsToCheck
                    )
                    else setFilePermissions(filePath, permissionsToCheck)
                }
            }
            // If there is not parentDirPath restriction or path is not in parentDirPath or
            // if existence or permission errors must not be ignored for paths in parentDirPath
            // If path is not a directory
            // Directories can be automatically created so we can ignore if missing with above check
            if (fileType != FileType.DIRECTORY) {
                return false
            }
            if (permissionsToCheck != null) {
                // Check if permissions are missing
                return checkMissingFilePermissions(
                    filePath,
                    permissionsToCheck
                )
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * Create a directory file at path.
     *
     *
     * This function is a wrapper for
     * [.validateDirectoryFileExistenceAndPermissions].
     *
     * are to be set or if they should be overridden.
     * @return Returns the `error` if path is not a directory file, failed to create it,
     * or validating permissions failed, otherwise `null`.
     */
    /**
     * Create a directory file at path.
     *
     *
     * This function is a wrapper for
     * [.validateDirectoryFileExistenceAndPermissions].
     *
     * @param filePath The `path` for directory file to create.
     * @return Returns the `error` if path is not a directory file or failed to create it,
     * otherwise `null`.
     */

    fun createDirectoryFile(
        filePath: String?,
        permissionsToCheck: String? = null,
        setPermissions: Boolean = false,
        setMissingPermissionsOnly: Boolean = false
    ): Boolean {
        return validateDirectoryFileExistenceAndPermissions(
            filePath,
            true,
            permissionsToCheck,
            setPermissions,
            setMissingPermissionsOnly
        )
    }

    /**
     * Delete file at path.
     *
     *
     * The `filePath` must be the canonical path to the file to be deleted since symlinks will
     * not be followed.
     * If the `filePath` is a canonical path to a directory, then any symlink files found under
     * the directory will be deleted, but not their targets.
     *
     * see if it should be deleted or not. This is a safety measure to
     * prevent accidental deletion of the wrong type of file, like a
     * directory instead of a regular file. You can pass
     * to allow deletion of any file type.
     * @return Returns the `error` if deletion was not successful, otherwise `null`.
     */
    /**
     * Delete regular, directory or symlink file at path.
     *
     *
     * This function is a wrapper for [.deleteFile].
     *
     * @param filePath              The `path` for file to delete.
     * @param ignoreNonExistentFile The `boolean` that decides if it should be considered an
     * error if file to deleted doesn't exist.
     * @return Returns the `error` if deletion was not successful, otherwise `null`.
     */

    fun deleteFile(
        filePath: String?,
        ignoreNonExistentFile: Boolean,
        ignoreWrongFileType: Boolean = false,
        allowedFileTypeFlags: Int = FileTypes.FILE_TYPE_NORMAL_FLAGS
    ): Boolean {
        if (filePath.isNullOrEmpty()) return false
        try {
            val file = File(filePath)
            var fileType = getFileType(filePath)
            // If file does not exist
            if (fileType == FileType.NO_EXIST) {
                // If delete is to be ignored if file does not exist
                return ignoreNonExistentFile  // Else return with error
            }
            // If the file type of the file does not exist in the allowedFileTypeFlags
            if ((allowedFileTypeFlags and fileType.value) <= 0) {
                // If wrong file type is to be ignored
                return ignoreWrongFileType
                // Else return with error
            }
            /*
             * Try to use {@link SecureDirectoryStream} if available for safer directory
             * deletion, it should be available for android >= 8.0
             * https://guava.dev/releases/24.1-jre/api/docs/com/google/common/io/MoreFiles.html#deleteRecursively-java.nio.file.Path-com.google.common.io.RecursiveDeleteOption...-
             * https://github.com/google/guava/issues/365
             * https://cs.android.com/android/platform/superproject/+/android-11.0.0_r3:libcore/ojluni/src/main/java/sun/nio/fs/UnixSecureDirectoryStream.java
             *
             * MoreUtils is marked with the @Beta annotation so the API may be removed in
             * future but has been there for a few years now.
             *
             * If an exception is thrown, the exception message might not contain the full errors.
             * Individual failures get added to suppressed throwables which can be extracted
             * from the exception object by calling `Throwable[] getSuppressed()`. So just logging
             * the exception message and stacktrace may not be enough, the suppressed throwables
             * need to be logged as well, which the Logger class does if they are found in the
             * exception added to the Error that's returned by this function.
             * https://github.com/google/guava/blob/v30.1.1/guava/src/com/google/common/io/MoreFiles.java#L775
             */
            file.deleteRecursively()

            // If file still exists after deleting it
            fileType = getFileType(filePath)
            if (fileType != FileType.NO_EXIST) return false
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * Clear contents of directory at path without deleting the directory. If directory does not exist
     * it will be created automatically.
     *
     *
     * The `filePath` must be the canonical path to a directory since symlinks will not be followed.
     * Any symlink files found under the directory will be deleted, but not their targets.
     *
     * @param filePath The `path` for directory to clear.
     * @return Returns the `error` if clearing was not successful, otherwise `null`.
     */
    fun clearDirectory(filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return false
        try {
            val file = File(filePath)
            val fileType = getFileType(filePath)
            // If file exists but not a directory file
            if (fileType != FileType.NO_EXIST && fileType != FileType.DIRECTORY) {
                return false
            }
            // If directory exists, clear its contents
            if (fileType == FileType.DIRECTORY) {
                /* If an exception is thrown, the exception message might not contain the full errors.
                 * Individual failures get added to suppressed throwables. */
                file.deleteRecursively()
            } else  // Else create it
            {
                return createDirectoryFile(filePath)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * Set permissions for file at path. Existing permission outside the `permissionsToSet`
     * will be removed.
     *
     * @param filePath         The `path` for file to set permissions to.
     * @param permissionsToSet The 3 character string that contains the "r", "w", "x" or "-" in-order.
     */
    private fun setFilePermissions(filePath: String?, permissionsToSet: String) {
        if (filePath.isNullOrEmpty()) return
        if (isValidPermissionString(permissionsToSet)) {
            return
        }
        val file = getFile(filePath, permissionsToSet)
        if (permissionsToSet.contains("x")) {
            if (!file.canExecute()) {
                file.setExecutable(true)
            }
        } else {
            if (file.canExecute()) {
                file.setExecutable(false)
            }
        }
    }


    private fun getFile(filePath: String, permissionsToSet: String): File {
        val file = File(filePath)
        if (permissionsToSet.contains("r")) {
            if (!file.canRead()) {
                file.setReadable(true)
            }
        } else {
            if (file.canRead()) {
                file.setReadable(false)
            }
        }
        if (permissionsToSet.contains("w")) {
            if (!file.canWrite()) {
                file.setWritable(true)
            }
        } else {
            if (file.canWrite()) {
                file.setWritable(false)
            }
        }
        return file
    }

    /**
     * Set missing permissions for file at path. Existing permission outside the `permissionsToSet`
     * will not be removed.
     *
     * @param filePath         The `path` for file to set permissions to.
     * @param permissionsToSet The 3 character string that contains the "r", "w", "x" or "-" in-order.
     */

    private fun setMissingFilePermissions(filePath: String?, permissionsToSet: String) {
        if (filePath.isNullOrEmpty()) return
        if (isValidPermissionString(permissionsToSet)) {
            return
        }
        val file = File(filePath)
        if (permissionsToSet.contains("r") && !file.canRead()) {
            file.setReadable(true)
        }
        if (permissionsToSet.contains("w") && !file.canWrite()) {
            file.setWritable(true)
        }
        if (permissionsToSet.contains("x") && !file.canExecute()) {
            file.setExecutable(true)
        }
    }

    /**
     * Checking missing permissions for file at path.
     *
     * @param filePath              The `path` for file to check permissions for.
     * @param permissionsToCheck    The 3 character string that contains the "r", "w", "x" or "-" in-order.
     * @return Returns the `error` if validating permissions failed, otherwise `null`.
     */

    private fun checkMissingFilePermissions(
        filePath: String?,
        permissionsToCheck: String
    ): Boolean {
        if (filePath.isNullOrEmpty()) return false
        if (isValidPermissionString(permissionsToCheck)) {
            return false
        }
        val file = File(filePath)
        // If file is not readable
        if (permissionsToCheck.contains("r") && !file.canRead()) {
            return false
        }
        // If file is not writable
        if (permissionsToCheck.contains("w") && !file.canWrite()) {
            return false
        } else  // If file is not executable
        // This canExecute() will give "avc: granted { execute }" warnings for target sdk 29
            if (permissionsToCheck.contains("x") && !file.canExecute()) {
                return false
            }
        return true
    }

    /**
     * Checks whether string exactly matches the 3 character permission string that
     * contains the "r", "w", "x" or "-" in-order.
     *
     * @param string The [String] to check.
     * @return Returns `true` if string exactly matches a permission string, otherwise `false`.
     */
    private fun isValidPermissionString(string: String?): Boolean {
        if (string.isNullOrEmpty()) return true
        return !Pattern.compile("^([r-])[w-][x-]$", 0).matcher(string).matches()
    }
    /**
     * Validate if [NyxConstants.TERMUX_FILES_DIR_PATH] exists and has
     * [FileUtils.APP_WORKING_DIRECTORY_PERMISSIONS] permissions.
     *
     *
     * This is required because binaries compiled for termux are hard coded with
     * [NyxConstants.TERMUX_PREFIX_DIR_PATH] and the path must be accessible.
     *
     *
     * The permissions set to directory will be [FileUtils.APP_WORKING_DIRECTORY_PERMISSIONS].
     *
     *
     * This function does not create the directory manually but by calling [Context.getFilesDir]
     * so that android itself creates it. However, the call will not create its parent package
     * data directory `/data/user/0/[package_name]` if it does not already exist and a `logcat`
     * error will be logged by android.
     * `Failed to ensure /data/user/0/<package_name>/files: mkdir failed: ENOENT (No such file or directory)`
     * An android app normally can't create the package data directory since its parent `/data/user/0`
     * is owned by `system` user and is normally created at app install or update time and not at app startup.
     *
     *
     * Note that the path returned by [Context.getFilesDir] may
     * be under `/data/user/[id]/[package_name]` instead of `/data/data/[package_name]`
     * defined by default by [NyxConstants.TERMUX_FILES_DIR_PATH] where id will be 0 for
     * primary user and a higher number for other users/profiles. If app is running under work profile
     * or secondary user, then [NyxConstants.TERMUX_FILES_DIR_PATH] will not be accessible
     * and will not be automatically created, unless there is a bind mount from `/data/data` to
     * `/data/user/[id]`, ideally in the right na[* https://source.android.com/devices/tech/](mespace.
      )admin/multi-user
     *
     *
     * On Android version `<=10`, the `/data/user/0` is a symlink to `/data/data[directory.
 * https://cs.android.com/android/platform/superproject/+/android-10.0.0_r47:system/core/r](`)ootdir/init.rc;l=589
     * `symlink /data/data /data/user/0
    ` *
     *
     *
     * `/system/bin/ls -lhd /data/data /data/user/0
     * drwxrwx--x 179 system system 8.0K 2021-xx-xx xx:xx /data/data
     * lrwxrwxrwx   1 root   root     10 2021-xx-xx xx:xx /data/user/0 -> /data/data
    ` *
     *
     *
     * On Android version `>=11`, the `/data/data` directory is bind mounted at `/d[* https://cs.android.com/android/platform/superproject/+/android-11.0.0_r40:system/core/r](ata/user/0`.
      )ootdir/i[* https://cs.android.com/android/_/android/platform/system/core/+/3cca270e95ca8d8bc8b8](nit.rc;l=705
      )00e2b5d7da1825fd7100
     * `# Unlink /data/user/0 if we previously symlink it to /data/data
     * rm /data/user/0
     * <p>
     * # Bind mount /data/user/0 to /data/data
     * mkdir /data/user/0 0700 system system encryption=None
     * mount none /data/data /data/user/0 bind rec
    ` *
     *
     *
     * `/system/bin/grep -E '( /data )|( /data/data )|( /data/user/[0-9]+ )' /proc/self/mountinfo 2>&1 | /system/bin/grep -v '/data_mirror' 2>&1
     * 87 32 253:5 / /data rw,nosuid,nodev,noatime shared:27 - ext4 /dev/block/dm-5 rw,seclabel,resgid=1065,errors=panic
     * 91 87 253:5 /data /data/user/0 rw,nosuid,nodev,noatime shared:27 - ext4 /dev/block/dm-5 rw,seclabel,resgid=1065,errors=panic
    ` *
     *
     *
     * The column 4 defines the root of the mount within the filesystem.
     * Basically, `/dev/block/dm-5/` is mounted at `/data` and `/dev/block/dm-5/data` is mounted at
     * [...]( `/data/user/0`.
      https://www.kernel.org/doc/Documentat)ion/filesystems/proc.t[(section 3.5)
 * https://www.kernel.org/doc/Documentation/files](xt)ystems/s[* https://unix.st](haredsubtree.txt
      )ackexchange.com/a/571959
     *
     *
     * Also note that running `/system/bin/ls -lhd /data/user/0/com.termux` as secondary user will result
     * in `ls: /data/user/0/com.termux: Permission denied` where `0` is primary user id but running
     * `/system/bin/ls -lhd /data/user/10/com.termux` will result in
     * `drwx------ 6 u10_a149 u10_a149 4.0K 2021-xx-xx xx:xx /data/user/10/com.termux` where `10` is
     * secondary user id. So can't stat directory (not contents) of primary user from secondary user
     * but can the other way around. However, this is happening on android 10 avd, but not on android
     * 11 avd.
     *
     * @param context                  The [Context] for operations.
     * @param createDirectoryIfMissing The `boolean` that decides if directory file
     * should be created if its missing.
     * @param setMissingPermissions    The `boolean` that decides if permissions are to be
     * automatically set.
     * @return Returns the `error` if path is not a directory file, failed to create it,
     * or validating permissions failed, otherwise `null`.
     */
    fun isTermuxFilesDirectoryAccessible(
        createDirectoryIfMissing: Boolean,
        setMissingPermissions: Boolean
    ): Boolean {
        if (createDirectoryIfMissing) File(NyxConstants.TERMUX_FILES_DIR_PATH).mkdirs()
        if (directoryFileExists()) return false
        if (setMissingPermissions) setMissingFilePermissions(
            NyxConstants.TERMUX_FILES_DIR_PATH,
            APP_WORKING_DIRECTORY_PERMISSIONS
        )
        return checkMissingFilePermissions(
            NyxConstants.TERMUX_FILES_DIR_PATH,
            APP_WORKING_DIRECTORY_PERMISSIONS
        )
    }

    /**
     * Validate if [NyxConstants.TERMUX_PREFIX_DIR_PATH] exists and has
     * [FileUtils.APP_WORKING_DIRECTORY_PERMISSIONS] permissions.
     * .
     *
     *
     * The [NyxConstants.TERMUX_PREFIX_DIR_PATH] directory would not exist if termux has
     * not been installed or the bootstrap setup has not been run or if it was deleted by the user.
     *
     * @param createDirectoryIfMissing The `boolean` that decides if directory file
     * should be created if its missing.
     * @param setMissingPermissions    The `boolean` that decides if permissions are to be
     * automatically set.
     * @return Returns the `error` if path is not a directory file, failed to create it,
     * or validating permissions failed, otherwise `null`.
     */
    fun isTermuxPrefixDirectoryAccessible(
        createDirectoryIfMissing: Boolean,
        setMissingPermissions: Boolean
    ): Boolean {
        return validateDirectoryFileExistenceAndPermissions(
            TERMUX_PREFIX_DIR_PATH,
            createDirectoryIfMissing,
            APP_WORKING_DIRECTORY_PERMISSIONS,
            setMissingPermissions,
            setMissingPermissionsOnly = true
        )
    }

    /**
     * Validate if [NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH] exists and has
     * [FileUtils.APP_WORKING_DIRECTORY_PERMISSIONS] permissions.
     *
     * @param createDirectoryIfMissing The `boolean` that decides if directory file
     * should be created if its missing.
     * @param setMissingPermissions    The `boolean` that decides if permissions are to be
     * automatically set.
     * @return Returns the `error` if path is not a directory file, failed to create it,
     * or validating permissions failed, otherwise `null`.
     */
    fun isTermuxPrefixStagingDirectoryAccessible(
        createDirectoryIfMissing: Boolean,
        setMissingPermissions: Boolean
    ): Boolean {
        return validateDirectoryFileExistenceAndPermissions(
            NyxConstants.TERMUX_STAGING_PREFIX_DIR_PATH,
            createDirectoryIfMissing,
            APP_WORKING_DIRECTORY_PERMISSIONS,
            setMissingPermissions,
            setMissingPermissionsOnly = true
        )
    }

    /**
     * Validate if [NyxConstants.TERMUX_APP.APPS_DIR_PATH] exists and has
     * [FileUtils.APP_WORKING_DIRECTORY_PERMISSIONS] permissions.
     *
     * @param createDirectoryIfMissing The `boolean` that decides if directory file
     * should be created if its missing.
     * @param setMissingPermissions    The `boolean` that decides if permissions are to be
     * automatically set.
     * @return Returns the `error` if path is not a directory file, failed to create it,
     * or validating permissions failed, otherwise `null`.
     */
    fun isAppsTermuxAppDirectoryAccessible(
        createDirectoryIfMissing: Boolean,
        setMissingPermissions: Boolean
    ): Boolean {
        return validateDirectoryFileExistenceAndPermissions(
            NyxConstants.TERMUX_APP.APPS_DIR_PATH,
            createDirectoryIfMissing,
            APP_WORKING_DIRECTORY_PERMISSIONS,
            setMissingPermissions,
            setMissingPermissionsOnly = true
        )
    }
}
