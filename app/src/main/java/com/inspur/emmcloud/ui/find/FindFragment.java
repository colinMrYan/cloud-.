package com.inspur.emmcloud.ui.find;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.inspur.reactnative.AuthorizationManagerPackage;
import com.reactnativecomponent.swiperefreshlayout.RCTSwipeRefreshLayoutPackage;


/**
 * com.inspur.emmcloud.ui.FindFragment create at 2016年8月29日 下午3:27:26
 */
public class FindFragment extends Fragment implements DefaultHardwareBackBtnHandler{
	private ReactRootView mReactRootView;
	private ReactInstanceManager mReactInstanceManager;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if(mReactRootView == null ){
			mReactRootView = new ReactRootView(getActivity());
			mReactInstanceManager = ReactInstanceManager.builder()
					.setApplication(getActivity().getApplication())
					.setCurrentActivity(getActivity())
					.setBundleAssetName("default/index.android.bundle")
					.setJSMainModuleName("index.android")
					.addPackage(new MainReactPackage())
					.addPackage(new RCTSwipeRefreshLayoutPackage())
					.addPackage(new AuthorizationManagerPackage())
					.setUseDeveloperSupport(BuildConfig.DEBUG)
					.setInitialLifecycleState(LifecycleState.RESUMED)
					.build();
			mReactRootView.startReactApplication(mReactInstanceManager, "discover", null);
		}
		return mReactRootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void invokeDefaultOnBackPressed() {
		getActivity().onBackPressed();
	}

}
