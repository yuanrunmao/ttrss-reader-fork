/*
 * Copyright (c) 2015, Nils Braden
 *
 * This file is part of ttrss-reader-fork. This program is free software; you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; If
 * not, see http://www.gnu.org/licenses/.
 */

package org.ttrssreader.gui.dialogs;

import org.ttrssreader.R;
import org.ttrssreader.controllers.Data;
import org.ttrssreader.model.pojos.Label;
import org.ttrssreader.model.updaters.IUpdatable;
import org.ttrssreader.model.updaters.Updater;
import org.ttrssreader.utils.LabelTitleComparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArticleLabelDialog extends MyDialogFragment {

	private static final String PARAM_ARTICLE_ID = "article_id";

	private int articleId;
	private List<Label> labels;

	private LinearLayout labelsView;

	public static ArticleLabelDialog newInstance(int articleId) {
		ArticleLabelDialog frag = new ArticleLabelDialog();
		Bundle args = new Bundle();
		args.putInt(PARAM_ARTICLE_ID, articleId);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle extras = getArguments();
		if (extras != null) {
			articleId = extras.getInt(PARAM_ARTICLE_ID);
		} else if (savedInstanceState != null) {
			articleId = savedInstanceState.getInt(PARAM_ARTICLE_ID);
		}

		// Put labels into list and sort by caption:
		labels = new ArrayList<>();
		for (Label label : Data.getInstance().getLabels(articleId)) {
			labels.add(label);
		}
		Collections.sort(labels, LabelTitleComparator.LABELTITLE_COMPARATOR);

		for (Label label : labels) {
			CheckBox checkbox = new CheckBox(getActivity());
			checkbox.setId(label.id);
			checkbox.setText(label.caption);
			checkbox.setChecked(label.checked);
			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (buttonView instanceof CheckBox) {
						CheckBox cb = (CheckBox) buttonView;
						for (Label label : labels) {
							if (label.id == cb.getId()) {
								label.checked = isChecked;
								label.checkedChanged = !label.checkedChanged;
								break;
							}
						}
					}
				}
			});
			labelsView.addView(checkbox);
		}

		if (labels.size() == 0) {
			TextView tv = new TextView(getActivity());
			tv.setText(R.string.Labels_NoLabels);
			labelsView.addView(tv);
		}
	}

	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle args) {
		// AboutDialog benutzt als Schriftfarbe automatisch die invertierte Hintergrundfarbe
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.articlelabeldialog, null);
		builder.setView(view).setPositiveButton(R.string.Utils_OkayAction, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				new Updater(null, new ArticleLabelUpdater()).execute();
				getActivity().setResult(Activity.RESULT_OK);
				dismiss();
			}
		}).setNegativeButton(R.string.Utils_CancelAction, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getActivity().setResult(Activity.RESULT_CANCELED);
				dismiss();
			}
		});

		labelsView = (LinearLayout) view.findViewById(R.id.labels);

		return builder.create();
	}

	private class ArticleLabelUpdater implements IUpdatable {
		@Override
		public void update() {
			for (Label label : labels) {
				if (label.checkedChanged) {
					Data.getInstance().setLabel(articleId, label);
				}
			}
		}
	}

}
