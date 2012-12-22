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

import org.json.JSONObject;

/**
 * Interface that implements some of the shim.js callbacks from the WebKit.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public interface ODKActivity {

	public void saveAllChangesCompleted(String tableId, String instanceId, boolean asComplete);

	public void saveAllChangesFailed(String tableId, String instanceId );

	public void ignoreAllChangesCompleted(String tableId, String instanceId);

	public void ignoreAllChangesFailed(String tableId, String instanceId);

	public String doAction( String page, String path, String action, JSONObject valueMap );

	/**
	 * Use the Activity implementation of this.
	 *
	 * @param r
	 */
	public void runOnUiThread(Runnable r);
}
