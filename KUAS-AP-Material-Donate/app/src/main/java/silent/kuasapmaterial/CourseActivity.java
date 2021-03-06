package silent.kuasapmaterial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kuas.ap.donate.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import silent.kuasapmaterial.base.SilentActivity;
import silent.kuasapmaterial.callback.CourseCallback;
import silent.kuasapmaterial.callback.SemesterCallback;
import silent.kuasapmaterial.libs.AlarmHelper;
import silent.kuasapmaterial.libs.Constant;
import silent.kuasapmaterial.libs.Helper;
import silent.kuasapmaterial.libs.MaterialProgressBar;
import silent.kuasapmaterial.libs.Memory;
import silent.kuasapmaterial.libs.Utils;
import silent.kuasapmaterial.models.CourseModel;
import silent.kuasapmaterial.models.SemesterModel;

public class CourseActivity extends SilentActivity implements SwipeRefreshLayout.OnRefreshListener {

	View mPickYmsView;
	ImageView mPickYmsImageView;
	TextView mNoCourseTextView, mHolidayTextView, mPickYmsTextView;
	LinearLayout mNoCourseLinearLayout;
	MaterialProgressBar mMaterialProgressBar;
	SwipeRefreshLayout mSwipeRefreshLayout;
	ScrollView mScrollView;

	String mYms;
	List<List<CourseModel>> mList;
	List<SemesterModel> mSemesterList;
	SemesterModel mSelectedModel;
	private int mPos = 0;
	private boolean isHoliday, isNight, isHolidayNight, isB, isHolidayB;
	boolean isRetry = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		setContentView(R.layout.activity_course);
		init(R.string.course, R.layout.activity_course, R.id.nav_course);

		initGA("Course Screen");
		restoreArgs(savedInstanceState);
		findViews();
		setUpViews();
	}

	@Override
	public void finish() {
		super.finish();

		overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	private void restoreArgs(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mYms = savedInstanceState.getString("mYms");
			mPos = savedInstanceState.getInt("mPos");
			isHoliday = savedInstanceState.getBoolean("isHoliday");
			isNight = savedInstanceState.getBoolean("isNight");
			isHolidayNight = savedInstanceState.getBoolean("isHolidayNight");
			isB = savedInstanceState.getBoolean("isB");
			isHolidayB = savedInstanceState.getBoolean("isHolidayB");
			isRetry = savedInstanceState.getBoolean("isRetry");

			if (savedInstanceState.containsKey("mList")) {
				mList = new Gson().fromJson(savedInstanceState.getString("mList"),
						new TypeToken<List<List<CourseModel>>>() {
						}.getType());
			}
			if (savedInstanceState.containsKey("mSelectedModel")) {
				mSelectedModel = new Gson().fromJson(savedInstanceState.getString("mSelectedModel"),
						new TypeToken<SemesterModel>() {
						}.getType());
			}
			if (savedInstanceState.containsKey("mSemesterList")) {
				mSemesterList = new Gson().fromJson(savedInstanceState.getString("mSemesterList"),
						new TypeToken<List<SemesterModel>>() {
						}.getType());
			}
		}

		if (mList == null) {
			mList = new ArrayList<>();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString("mYms", mYms);
		outState.putBoolean("isHoliday", isHoliday);
		outState.putBoolean("isNight", isNight);
		outState.putBoolean("isHolidayNight", isHolidayNight);
		outState.putBoolean("isB", isB);
		outState.putBoolean("isHolidayB", isHolidayB);
		outState.putBoolean("isRetry", isRetry);
		if (mScrollView != null) {
			outState.putInt("mPos", mScrollView.getVerticalScrollbarPosition());
		}
		if (mList != null) {
			outState.putString("mList", new Gson().toJson(mList));
		}
		if (mSelectedModel != null) {
			outState.putString("mSelectedModel", new Gson().toJson(mSelectedModel));
		}
		if (mSemesterList != null) {
			outState.putString("mSemesterList", new Gson().toJson(mSemesterList));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case Constant.REQUEST_PICK_SEMESTER:
				if (resultCode == RESULT_OK && data != null) {
					if (data.hasExtra("mSelectedModel")) {
						mSelectedModel = new Gson().fromJson(data.getStringExtra("mSelectedModel"),
								new TypeToken<SemesterModel>() {
								}.getType());
						mYms = mSelectedModel.value;
						mPickYmsTextView.setText(mSelectedModel.text);
						getData(false);
					}
				}
				break;
		}
	}

	private void getSemester() {
		Helper.getSemester(this, new SemesterCallback() {

			@Override
			public void onSuccess(List<SemesterModel> modelList, SemesterModel selectedModel) {
				super.onSuccess(modelList, selectedModel);
				mSemesterList = modelList;
				mSelectedModel = selectedModel;
				mYms = mSelectedModel.value;
				mPickYmsTextView.setText(mSelectedModel.text);
				getData(true);
			}

			@Override
			public void onFail(String errorMessage) {
				super.onFail(errorMessage);
				isRetry = true;
				setUpCourseTable();
			}
		});
	}

	private void findViews() {
		mScrollView = (ScrollView) findViewById(R.id.scrollView);
		mPickYmsTextView = (TextView) findViewById(R.id.textView_pickYms);
		mPickYmsView = findViewById(R.id.view_pickYms);
		mPickYmsImageView = (ImageView) findViewById(R.id.imageView_pickYms);
		mMaterialProgressBar = (MaterialProgressBar) findViewById(R.id.materialProgressBar);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		mNoCourseLinearLayout = (LinearLayout) findViewById(R.id.linearLayout_no_course);
		mNoCourseTextView = (TextView) findViewById(R.id.textView_no_course);
		mHolidayTextView = (TextView) findViewById(R.id.textView_holiday);
	}

	private void setUpViews() {
		setUpPullRefresh();
		mHolidayTextView.setText(getString(R.string.course_holiday, "\uD83D\uDE06"));

		Bitmap sourceBitmap = Utils.convertDrawableToBitmap(
				ContextCompat.getDrawable(this, R.drawable.ic_keyboard_arrow_down_white_24dp));
		int color = ContextCompat.getColor(this, R.color.accent);
		mPickYmsImageView.setImageBitmap(Utils.changeImageColor(sourceBitmap, color));

		mScrollView.scrollTo(0, mPos);
		mPickYmsView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSelectedModel == null) {
					return;
				}
				mTracker.send(
						new HitBuilders.EventBuilder().setCategory("pick yms").setAction("click")
								.build());
				Intent intent = new Intent(CourseActivity.this, PickSemesterActivity.class);
				intent.putExtra("mSemesterList", new Gson().toJson(mSemesterList));
				intent.putExtra("mSelectedModel", new Gson().toJson(mSelectedModel));
				startActivityForResult(intent, Constant.REQUEST_PICK_SEMESTER);
			}
		});
		mNoCourseLinearLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isRetry) {
					mTracker.send(
							new HitBuilders.EventBuilder().setCategory("retry").setAction("click")
									.setLabel((mSemesterList == null) + "").build());
					isRetry = false;
					if (mSemesterList == null || mSelectedModel == null) {
						getSemester();
					} else {
						getData(false);
					}
				} else {
					if (mSemesterList == null || mSelectedModel == null) {
						getSemester();
						return;
					}
					mTracker.send(new HitBuilders.EventBuilder().setCategory("pick yms")
							.setAction("click").build());
					Intent intent = new Intent(CourseActivity.this, PickSemesterActivity.class);
					intent.putExtra("mSemesterList", new Gson().toJson(mSemesterList));
					intent.putExtra("mSelectedModel", new Gson().toJson(mSelectedModel));
					startActivityForResult(intent, Constant.REQUEST_PICK_SEMESTER);
				}
			}
		});

		if (mSelectedModel != null && mSemesterList != null) {
			mPickYmsTextView.setText(mSelectedModel.text);
			setUpCourseTable();
		} else {
			mPickYmsView.setEnabled(false);
			getSemester();
		}
	}

	@Override
	public void onRefresh() {
		if (mYms != null) {
			mTracker.send(new HitBuilders.EventBuilder().setCategory("refresh").setAction("swipe")
					.build());
			isRetry = false;
			mSwipeRefreshLayout.setRefreshing(true);
			getData(false);
		}
	}

	private void setUpPullRefresh() {
		mSwipeRefreshLayout.setOnRefreshListener(this);
		mSwipeRefreshLayout.setColorSchemeColors(Utils.getSwipeRefreshColors(this));
	}

	private void getData(final boolean isSave) {
		if (!mSwipeRefreshLayout.isRefreshing()) {
			mMaterialProgressBar.setVisibility(View.VISIBLE);
		}
		mPickYmsView.setEnabled(false);
		mScrollView.setVisibility(View.GONE);
		mNoCourseLinearLayout.setVisibility(View.GONE);
		mHolidayTextView.setVisibility(View.GONE);
		mSwipeRefreshLayout.setEnabled(false);

		Helper.getCourseTimeTable(this, mYms.split(",")[0], mYms.split(",")[1],
				new CourseCallback() {

					@Override
					public void onSuccess(List<List<CourseModel>> modelList) {
						super.onSuccess(modelList);

						if (isSave &&
								Memory.getBoolean(CourseActivity.this, Constant.PREF_COURSE_NOTIFY,
										false)) {
							AlarmHelper.setCourseNotification(CourseActivity.this, modelList);
						}

						mList = modelList;
						setUpCourseTable();
						mPickYmsView.setEnabled(true);
					}

					@Override
					public void onFail(String errorMessage) {
						super.onFail(errorMessage);

						mList.clear();
						isRetry = true;
						setUpCourseTable();
						mPickYmsView.setEnabled(true);
					}

					@Override
					public void onTokenExpired() {
						super.onTokenExpired();
						Utils.createTokenExpired(CourseActivity.this).show();
						mTracker.send(new HitBuilders.EventBuilder().setCategory("token")
								.setAction("expired").build());
					}
				});
	}

	private void setUpCourseTable() {
		isHoliday = false;
		isNight = false;
		isHolidayNight = false;
		isB = false;
		isHolidayB = false;

		mScrollView.removeAllViews();
		if (mList.size() == 0) {
			if (isRetry) {
				mNoCourseTextView.setText(R.string.click_to_retry);
			} else {
				mNoCourseTextView.setText(getString(R.string.course_no_course, "\uD83D\uDE0B"));
			}
			mMaterialProgressBar.setVisibility(View.GONE);
			mSwipeRefreshLayout.setEnabled(true);
			mSwipeRefreshLayout.setRefreshing(false);
			mNoCourseLinearLayout.setVisibility(View.VISIBLE);
			mScrollView.setVisibility(View.VISIBLE);
			mHolidayTextView.setVisibility(View.GONE);
			return;
		} else {
			mNoCourseLinearLayout.setVisibility(View.GONE);
		}
		checkCourseTableType();
		TableLayout table = selectCourseTable();

		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i) != null) {
				for (int j = 0; j < mList.get(i).size(); j++) {
					int id = getResources()
							.getIdentifier("textView" + j + "_" + (i + 1), "id", getPackageName());
					final TextView courseTextView = (TextView) table.findViewById(id);
					if (mList.get(i).get(j) != null) {
						if (courseTextView == null) {
							continue;
						}
						courseTextView.setText(mList.get(i).get(j).title.substring(0, 2));

						final int weekday = i;
						final int section = j;
						courseTextView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								showCourseDialog(weekday, section);
							}
						});
					} else {
						if (courseTextView != null) {
							courseTextView.setText("　　");
						}
					}
				}
			} else {
				List<String> sections = new ArrayList<>(
						Arrays.asList(getResources().getStringArray(R.array.course_sections)));
				for (int j = 0; j < sections.size(); j++) {
					int id = getResources()
							.getIdentifier("textView" + j + "_" + (i + 1), "id", getPackageName());
					final TextView courseTextView = (TextView) table.findViewById(id);
					if (courseTextView != null) {
						courseTextView.setText("　　");
					}
				}
			}
		}

		mScrollView.addView(table);
		mMaterialProgressBar.setVisibility(View.GONE);
		mSwipeRefreshLayout.setEnabled(true);
		mSwipeRefreshLayout.setRefreshing(false);
		mScrollView.setVisibility(View.VISIBLE);
	}

	private void showCourseDialog(final int weekday, final int section) {
		mTracker.send(new HitBuilders.EventBuilder().setCategory("show course").setAction("click")
				.setLabel(mList.get(weekday).get(section).title).build());

		String instructors = mList.get(weekday).get(section).instructors.size() > 0 ?
				mList.get(weekday).get(section).instructors.get(0) : "";
		for (int k = 1; k < mList.get(weekday).get(section).instructors.size(); k++) {
			instructors += "," + mList.get(weekday).get(section).instructors.get(k);
		}

		String start_time = !mList.get(weekday).get(section).start_time.contains(":") ?
				getResources().getStringArray(R.array.start_time)[section] :
				mList.get(weekday).get(section).start_time;
		String end_time = !mList.get(weekday).get(section).end_time.contains(":") ?
				getResources().getStringArray(R.array.end_time)[section] :
				mList.get(weekday).get(section).end_time;

		new AlertDialog.Builder(CourseActivity.this).setTitle(R.string.course_dialog_title)
				.setMessage(getString(R.string.course_dialog_messages,
						mList.get(weekday).get(section).title, instructors,
						mList.get(weekday).get(section).room, start_time +
								" - " + end_time)).setPositiveButton(R.string.ok, null).show();
	}

	private void checkCourseTableType() {
		for (int i = 0; i < mList.size() && !(isHolidayNight && isHoliday &&
				isNight && isHolidayB && isB); i++) {
			if (mList.get(i) != null) {
				if (i > 4) {
					isHoliday = true;
				}
				for (int j = 0; j < mList.get(i).size() && !(isHolidayNight && isHoliday &&
						isNight && isHolidayB && isB); j++) {
					if (mList.get(i).get(j) != null) {
						if (j > 10) {
							if (i > 4) {
								isHolidayNight = true;
							} else {
								isNight = true;
							}
						} else if (j == 10) {
							if (i > 4) {
								isHolidayB = true;
							} else {
								isB = true;
							}
						}
					}
				}
			}
		}
	}

	private TableLayout selectCourseTable() {
		if ((Utils.isWide(this) || Utils.isLand(this)) && isHoliday) {
			if (isHolidayNight || isNight) {
				return (TableLayout) LayoutInflater.from(this)
						.inflate(R.layout.table_course_holiday_night, null);
			} else if (isHolidayB || isB) {
				return (TableLayout) LayoutInflater.from(this)
						.inflate(R.layout.table_course_holiday_b, null);
			} else {
				return (TableLayout) LayoutInflater.from(this)
						.inflate(R.layout.table_course_holiday, null);
			}
		} else {
			if (isHoliday) {
				mHolidayTextView.setVisibility(View.VISIBLE);
			} else {
				mHolidayTextView.setVisibility(View.GONE);
			}
			if (isNight) {
				return (TableLayout) LayoutInflater.from(this)
						.inflate(R.layout.table_course_normal_night, null);
			} else if (isB) {
				return (TableLayout) LayoutInflater.from(this)
						.inflate(R.layout.table_course_normal_b, null);
			} else {
				return (TableLayout) LayoutInflater.from(this)
						.inflate(R.layout.table_course_normal, null);
			}
		}
	}
}
