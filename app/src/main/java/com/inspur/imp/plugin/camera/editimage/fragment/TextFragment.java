package com.inspur.imp.plugin.camera.editimage.fragment;

import android.support.v4.app.Fragment;

/**
 * 贴文字分类fragment
 * @author alafighting
 */
public class TextFragment extends Fragment {
//    public static final String TAG = TextFragment.class.getName();
//    public static final String FONT_FOLDER = "fonts";
//
//    private View mainView;
//    private EditImageActivity activity;
//    private LabelTextView mLableTextView;// 文字显示控件
//    private RecyclerView recyclerView;
//    private FontTypeAdapter fontTypeAdapter;
//    private View backToMenu;// 返回主菜单
//    private List<String> fonts = new ArrayList<>();
//
//    private AlertDialog inputDialog;
//    private View dialogView;
//    private EditText input;
//    private Button sureBtn;
//    private Typeface inputTypeFace;
//
//    public static TextFragment newInstance(EditImageActivity activity) {
//        TextFragment fragment = new TextFragment();
//        fragment.activity = activity;
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        mainView = inflater.inflate(R.layout.fragment_edit_image_text, null);
//        this.mLableTextView = activity.mTextPanel;
//
//        backToMenu = mainView.findViewById(R.id.back_to_main);
//        recyclerView = (RecyclerView) mainView.findViewById(R.id.font_type_list);
//
//        fontTypeAdapter = new FontTypeAdapter(activity.getAssets(), fonts);
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
//        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setAdapter(fontTypeAdapter);
//        fontTypeAdapter.setOnItemClickListener(new FontTypeAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(Typeface typeface) {
//                if (typeface != null) {
//                    inputTypeFace = typeface;
//                    showDialog();
//                }
//            }
//        });
//        return mainView;
//    }
//
//    public void showDialog() {
//        if (inputDialog == null) {
//            dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_input, null);
//            input = (EditText) dialogView.findViewById(R.id.input);
//            sureBtn = (Button) dialogView.findViewById(R.id.sureBtn);
//            sureBtn.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (!TextUtils.isEmpty(input.getText().toString())) {
//                        selectedStickerItem(inputTypeFace, input.getText().toString());
//                        input.setText("");
//                        inputDialog.dismiss();
//                    } else {
//                        Toast.makeText(activity, "输入内容不为空", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//            inputDialog = new AlertDialog.Builder(activity)
//                    .setView(dialogView)
//                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            input.setText("");
//                        }}).create();
//            inputDialog.show();
//        } else {
//            inputDialog.show();
//        }
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        backToMenu.setOnClickListener(new BackToMenuClick());// 返回主菜单
//
//        loadData();
//    }
//
//    public void loadData() {
//        try {
//            String[] pathArray = activity.getAssets().list(FONT_FOLDER);
//            for (int i = 0; i < pathArray.length; i++) {
//                String tempPath = FONT_FOLDER + File.separator + pathArray[i];
//                fonts.add(tempPath);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            fontTypeAdapter.notifyDataSetChanged();
//        }
//    }
//
//    public LabelTextView getLableTextView() {
//        return mLableTextView;
//    }
//
//    public void setLableTextView(LabelTextView mLableTextView) {
//        this.mLableTextView = mLableTextView;
//    }
//
//    /**
//     * 选择贴图加入到页面中
//     *
//     * @param path
//     */
//    public void selectedStickerItem(Typeface typeface, String path) {
//        mLableTextView.addText(typeface, path);
//    }
//
//    /**
//     * 返回主菜单页面
//     *
//     * @author panyi
//     */
//    private final class BackToMenuClick implements OnClickListener {
//        @Override
//        public void onClick(View v) {
//            backToMain();
//        }
//    }// end inner class
//
//    public void backToMain() {
//        activity.mode = EditImageActivity.MODE_NONE;
//        activity.bottomGallery.setCurrentItem(0);
//        mLableTextView.setVisibility(View.GONE);
//        activity.bannerFlipper.showPrevious();
//    }
//
//    /**
//     * 保存贴图任务
//     *
//     * @author panyi
//     */
//    private final class SaveTextTask extends
//            AsyncTask<Bitmap, Void, Bitmap> {
//        private Dialog dialog;
//
//        @Override
//        protected Bitmap doInBackground(Bitmap... params) {
//            // System.out.println("保存贴图!");
//            Matrix touchMatrix = activity.mainImage.getImageViewMatrix();
//
//            Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(Bitmap.Config.RGB_565, true);
//            Canvas canvas = new Canvas(resultBit);
//
//            LinkedHashMap<Integer, TextItem> addItems = mLableTextView.getBank();
//            for (Integer id : addItems.keySet()) {
//                TextItem item = addItems.get(id);
//                // 输出文本到图片，合成新的图片
//                item.drawText(canvas, touchMatrix);
//            }// end for
//            saveBitmap(resultBit, activity.saveFilePath);
//            return resultBit;
//        }
//
//        @Override
//        protected void onCancelled() {
//            super.onCancelled();
//            dialog.dismiss();
//        }
//
//        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//        @Override
//        protected void onCancelled(Bitmap result) {
//            super.onCancelled(result);
//            dialog.dismiss();
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap result) {
//            super.onPostExecute(result);
//            mLableTextView.clear();
//            activity.changeMainBitmap(result);
//            dialog.dismiss();
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            dialog = BaseActivity.getLoadingDialog(getActivity(), "图片合成保存中...",
//                    false);
//            dialog.show();
//        }
//    }// end inner class
//
//    /**
//     * 保存贴图层 合成一张图片
//     */
//    public void saveTextSticker() {
//        // System.out.println("保存 合成图片");
//        SaveTextTask task = new SaveTextTask();
//        task.execute(activity.mainBitmap);
//    }
//
//    /**
//     * 保存Bitmap图片到指定文件
//     *
//     * @param bm
//     */
//    public static void saveBitmap(Bitmap bm, String filePath) {
//        File f = new File(filePath);
//        if (f.exists()) {
//            f.delete();
//        }
//        try {
//            FileOutputStream out = new FileOutputStream(f);
//            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.flush();
//            out.close();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        // System.out.println("保存文件--->" + f.getAbsolutePath());
//    }

}// end class
