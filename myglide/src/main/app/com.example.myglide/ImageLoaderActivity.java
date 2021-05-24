package com.example.myglide;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class ImageLoaderActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private String[] mImageUrls = {
            "http://b.hiphotos.baidu.com/zhidao/pic/item/a6efce1b9d16fdfafee0cfb5b68f8c5495ee7bd8.jpg",
            "https://hbimg.huabanimg.com/c9368f9172fe02d915d02fff60359c4a4e836765186a8-UxjKkj_fw658",
            "https://hbimg.huabanimg.com/e96b72cd16b8bbd877091a1497e0c7a8187c8ce4346c8-QcE6As_fw658",
            "https://hbimg.huabanimg.com/569e24a4ea43f149be9ae4badb97933a3fbcaacd1d54fe-7hg49f_fw658",
            "https://hbimg.huabanimg.com/7099ba1402cf825869b6e3659257db8a3d1844c62df50-XyDsON_fw658",
            "https://hbimg.huabanimg.com/116f17912c3fe74fb8856be053a338a72fb4c6d6456a15-pXETnz_fw658",
            "https://hbimg.huabanimg.com/e15d9b2c5e5ef7a1011116666fe477c776d5296e1201b-g3QGAw_fw658",
            "https://hbimg.huabanimg.com/5cbc91206c7e28f5b7800f8c4281921af911902722ef5-elx780_fw658",
            "http://img2.3lian.com/2014/c7/51/d/26.jpg",
            "http://img3.3lian.com/2013/c1/34/d/93.jpg",
            "https://hbimg.huabanimg.com/829986f5797852630c391726243f2df6be21e9cebee96-uvXcXW_fw658",
            "https://hbimg.huabanimg.com/40b80c5793fa84a2586c33e70bfdc8285f93dd3b1414b-PNRxKY_fw658",
            "http://cdn.duitang.com/uploads/item/201311/03/20131103171224_rr2aL.jpeg",
            "http://imgrt.pconline.com.cn/images/upload/upc/tx/wallpaper/1210/17/c1/spcgroup/14468225_1350443478079_1680x1050",
            "https://hbimg.huabanimg.com/018142216b1b9c3e374b056e8afd9adc7b2d4ea320a792-Y0Cq2r_fw658",
            "http://www.1tong.com/uploads/wallpaper/landscapes/200-4-730x456.jpg",
            "https://hbimg.huabanimg.com/76545c1c10a288bf483b13f5a5d091c108f794ed3607b6-TXY39K_fw658",
            "https://hbimg.huabanimg.com/2a7f54ced5dfeb24de452b430ec842214bff588361298-rU0eRG_fw658",
            "http://h.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=429e7b1b92ef76c6d087f32fa826d1cc/7acb0a46f21fbe09cc206a2e69600c338744ad8a.jpg",
            "https://hbimg.huabanimg.com/6c69ddbf157248be73007b6f401e403932ba430c472f-HYmuJ0_fw658",
            "http://cdn.duitang.com/uploads/item/201405/13/20140513212305_XcKLG.jpeg",
            "http://photo.loveyd.com/uploads/allimg/080618/1110324.jpg",
            "https://hbimg.huabanimg.com/3494d7658d3805116bb1aaf721713a77bbabac7627c72-VbUYs2_fw658",
            "http://cdn.duitang.com/uploads/item/201204/21/20120421155228_i52eX.thumb.600_0.jpeg",
            "https://hbimg.huabanimg.com/3286081ba1b365db5cc54112cb1f4dc3238ffceb5fbe-aJMH3i_fw658",
            "https://hbimg.huabanimg.com/3f10f8e1fb0471634666c4f534e1d626853bb33a132b6d-yfWLMT_fw658",
            "https://hbimg.huabanimg.com/6c3a58fe71f88d175eccd0e80d2e9d2869f411f61f0a1-C6wTCp_fw658",
            "https://hbimg.huabanimg.com/8a573a413d9a94fdfaf3001568372b0e3c0dec642c73dd-lnnbBu_fw658",
            "http://a.hiphotos.baidu.com/image/pic/item/a8773912b31bb051a862339c337adab44bede0c4.jpg",
            "https://hbimg.huabanimg.com/2f790d8270e9b1487b565a6ef2c643ff47063cafe162-aW67di_fw658",
            "http://img0.pconline.com.cn/pconline/bizi/desktop/1412/ER2.jpg",
            "https://hbimg.huabanimg.com/81f5a3f91bae934b4d5ca19e1a7b6a9817d1859c113002-bce0pL_fw658",
            "https://hbimg.huabanimg.com/0588fd50c893782b7aa483a1f4093414a784028cd137-3aeLbK_fw658",
            "https://hbimg.huabanimg.com/2374f99d76c2c5768e25ec018d7d755fb93931d623a6dc-IHuxU8_fw658",
            "http://img02.tooopen.com/images/20140320/sy_57121781945.jpg",
            "https://hbimg.huabanimg.com/a72218820fed15c6a622bfb2d49f5705ec827b7dd219-5n4pXV_fw658"

    };
    private List<String> mUrList = Arrays.asList(mImageUrls);
    private GridView mImageGridView;
    private BaseAdapter mImageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_loader);
        initView();
    }

    private void initView() {
        mImageGridView = findViewById(R.id.gridView1);
        mImageAdapter = new ImageAdapter(this);
        mImageGridView.setAdapter(mImageAdapter);
    }


    private class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ImageAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mUrList.size();
        }

        @Override
        public String getItem(int position) {
            return mUrList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_grid_view, parent, false);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.image_view);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MyGlide.with(convertView.getContext())
                    .load(getItem(position))
                    .placeholder(R.drawable.girl)
                    .error(R.drawable.error)
                    .into(holder.imageView);
            return convertView;
        }
    }

    private static class ViewHolder {
        public ImageView imageView;
    }

}
