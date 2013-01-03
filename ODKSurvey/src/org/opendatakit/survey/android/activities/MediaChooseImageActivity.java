/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.survey.android.activities;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.opendatakit.survey.android.R;
import org.opendatakit.survey.android.provider.FileProvider;
import org.opendatakit.survey.android.utilities.MediaUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.widget.Toast;

/**
 * Simple shim for media interactions.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class MediaChooseImageActivity extends Activity {
	private static final String t = "MediaChooseImageActivity";
    private static final int ACTION_CODE = 1;
    private static final String MEDIA_CLASS = "image/";
	private static final String URI = "uri";
	private static final String CONTENT_TYPE = "contentType";

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType(MEDIA_CLASS + "*");
        try {
            startActivityForResult(i, ACTION_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                getString(R.string.activity_not_found,
                		Intent.ACTION_GET_CONTENT + " " + MEDIA_CLASS),
                Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (resultCode == Activity.RESULT_CANCELED) {
            // request was canceled -- propagate
			setResult(Activity.RESULT_CANCELED);
			finish();
            return;
        }

		/*
         * We have chosen a saved image from somewhere, but we really want it to be in:
         * /sdcard/odk/instances/[current instance]/something.jpg so we copy it there
         * and insert that copy into the content provider.
         */

        // get gp of chosen file
        Uri selectedMedia = intent.getData();
        String sourceMediaPath = MediaUtils.getPathFromUri(selectedMedia, Images.Media.DATA);
        File sourceMedia = new File(sourceMediaPath);
        String extension = sourceMediaPath.substring(sourceMediaPath.lastIndexOf("."));

        String destMediaPath = MainMenuActivity.getInstanceFilePath(extension);

        File newMedia = new File(destMediaPath);
		try {
			FileUtils.copyFile(sourceMedia, newMedia);
		} catch (IOException e) {
    		Log.e(t, "Failed to copy " + sourceMedia.getAbsolutePath());
            Toast.makeText(this, R.string.media_save_failed, Toast.LENGTH_SHORT).show();
            // keep the image as a captured image so user can choose it.
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
		}

		Log.i(t, "copied " + sourceMedia.getAbsolutePath() + " to " + newMedia.getAbsolutePath());

		Uri mediaURI = null;
        if (newMedia.exists()) {
            // Add the new image to the Media content provider so that the
            // viewing is fast in Android 2.0+
        	ContentValues values = new ContentValues(6);
            values.put(Images.Media.TITLE, newMedia.getName());
            values.put(Images.Media.DISPLAY_NAME, newMedia.getName());
            values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(Images.Media.MIME_TYPE, MEDIA_CLASS + extension.substring(1));
            values.put(Images.Media.DATA, newMedia.getAbsolutePath());

            mediaURI = getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
            Log.i(t, "Insert " + MEDIA_CLASS + " returned uri = " + mediaURI.toString());

			// if you are replacing an answer. delete the previous image using the
	        // content provider.
	        String binarypath = MediaUtils.getPathFromUri(mediaURI, Images.Media.DATA);
	        File newMediaFromCP = new File(binarypath);

	        Log.i(t, "Return mediaFile: " + newMediaFromCP.getAbsolutePath());
	        Intent i = new Intent();
			i.putExtra(URI, FileProvider.getAsUrl(newMediaFromCP));
			String name = newMediaFromCP.getName();
			i.putExtra(CONTENT_TYPE, MEDIA_CLASS + name.substring(name.lastIndexOf(".") + 1));
            setResult(Activity.RESULT_OK, i);
            finish();
        } else {
            Log.e(t, "No " + MEDIA_CLASS + " exists at: " + newMedia.getAbsolutePath());
            Toast.makeText(this, R.string.media_save_failed, Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

}
