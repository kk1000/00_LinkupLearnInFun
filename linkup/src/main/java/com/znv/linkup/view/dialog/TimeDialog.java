package com.znv.linkup.view.dialog;

import android.app.Dialog;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.znv.linkup.GameActivity;
import com.znv.linkup.R;
import com.znv.linkup.WelcomeActivity;
import com.znv.linkup.db.DbScore;
import com.znv.linkup.db.LevelScore;
import com.znv.linkup.rest.IUpload;
import com.znv.linkup.rest.LevelInfo;
import com.znv.linkup.rest.UserInfo;
import com.znv.linkup.rest.UserScore;
import com.znv.linkup.util.ShareUtil;
import com.znv.linkup.util.StringUtil;
import com.znv.linkup.view.LevelTop;
import com.znv.linkup.view.LevelTop.LevelTopStatus;

/**
 * 计时模式结果
 * 
 * @author yzb
 * 
 */
public class TimeDialog extends Dialog implements IUpload {

    private GameActivity linkup = null;
    private ResultInfo resultInfo = null;
    private LevelTop levelTop = null;
    private ShareUtil shareHelper = null;

    public TimeDialog(final GameActivity linkup) {
        super(linkup, R.style.CustomDialogStyle);
        this.linkup = linkup;
        setContentView(R.layout.time_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        shareHelper = new ShareUtil(getContext());

        TextView btnBack = (TextView) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                levelTop.cancelUrlImages();
                cancel();
                linkup.onBackPressed();
            }

        });

        TextView btnShare = (TextView) findViewById(R.id.btnshare);
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String msg = String.format(getContext().getString(R.string.share_time), linkup.getLevelCfg().getLevelName(),
                        StringUtil.secondToString(resultInfo.getTime()));
                // 分享
                if (levelTop.getTopStatus() == LevelTopStatus.TopInfo) {
                    // 带截图分享
                    shareHelper.shareMsgView(msg, levelTop);
                } else {

                    shareHelper.shareMessage(msg);
                }
            }
        });

        TextView btnAgain = (TextView) findViewById(R.id.btnAgain);
        btnAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                levelTop.cancelUrlImages();
                cancel();
                linkup.start();
            }
        });

        TextView btnNext = (TextView) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                levelTop.cancelUrlImages();
                cancel();
                linkup.next();
            }
        });

        levelTop = (LevelTop) findViewById(R.id.time_top);
        levelTop.setUploadListener(this);
    }

    /**
     * 处理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            TextView btn = (TextView) findViewById(R.id.btnBack);
            btn.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 显示游戏胜利对话框
     * 
     * @param resultInfo
     *            游戏结果
     */
    public void showDialog(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        TextView tvTime = (TextView) findViewById(R.id.success_time);
        tvTime.setText(StringUtil.secondToString(resultInfo.getTime()) + getContext().getString(R.string.time_unit));
        TextView tvRecord = (TextView) findViewById(R.id.time_record);
        tvRecord.setText(StringUtil.secondToString(linkup.getLevelCfg().getMinTime()) + getContext().getString(R.string.time_unit));
        ImageView ivRecord = (ImageView) findViewById(R.id.level_champion);
        TextView tvDiamond = (TextView) findViewById(R.id.level_diamond_reward);
        tvDiamond.setText("+" + String.valueOf(resultInfo.getStars()));
        TextView tvCoin = (TextView) findViewById(R.id.level_coin_reward);
        tvCoin.setText("+" + String.valueOf(resultInfo.getScore() / 10));
        ivRecord.setVisibility(View.INVISIBLE);
        if (resultInfo.isNewRecord()) {
            ivRecord.setVisibility(View.VISIBLE);
        }

        if (levelTop != null) {
            levelTop.reset();
        }
        uploadTime();

        show();
    }

    /**
     * 上传时间
     */
    private void uploadTime() {
        // 判断是否已登录
        if (!resultInfo.getUserId().equals("")) {
            LevelInfo timeInfo = new LevelInfo();
            timeInfo.setUserId(resultInfo.getUserId());
            timeInfo.setLevel(resultInfo.getLevel());
            timeInfo.setDiamond(resultInfo.getStars());
            timeInfo.setGold(resultInfo.getScore() / 10);

            // 增加奖励的钻石和金币
            WelcomeActivity.userInfo.addDiamond(getContext(), timeInfo.getDiamond());
            if (resultInfo.isNewRecord()) {
                timeInfo.setScore(resultInfo.getScore());
                timeInfo.setTime(resultInfo.getTime());
                WelcomeActivity.userInfo.addGold(getContext(), timeInfo.getGold());
                UserScore.addGetResult(timeInfo, levelTop.netMsgHandler);
            } else {
                if (!resultInfo.isUpload()) {
                    timeInfo.setScore(resultInfo.getMaxScore());
                    timeInfo.setTime(resultInfo.getMinTime());
                    WelcomeActivity.userInfo.addGold(getContext(), timeInfo.getGold());
                    UserScore.addGetResult(timeInfo, levelTop.netMsgHandler);
                } else {
                    // 获取排行榜
                    UserScore.getLevelTops(resultInfo.getLevel(), levelTop.netMsgHandler);
                }
            }
        } else {
            // 没有登录则提示登录
        }
    }

    @Override
    public void onLoginSuccess(Message msg) {
        UserInfo userInfo = WelcomeActivity.userInfo;
        if (userInfo != null) {
            resultInfo.setUserId(userInfo.getUserId());
            uploadTime();
        }
    }

    @Override
    public void onLevelResultAdd(Message msg) {
        // 更新是否已上传
        linkup.getLevelCfg().setUpload(true);
        LevelScore ls = new LevelScore(resultInfo.getLevel());
        ls.setIsUpload(1);
        DbScore.updateUpload(ls);

        // 获取排行榜
        // UserScore.getLevelTops(resultInfo.getLevel(), levelTop.netMsgHandler);
    }
}
