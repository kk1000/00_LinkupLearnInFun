package com.znv.linkup.view.dialog;

import android.app.Dialog;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.znv.linkup.GameActivity;
import com.znv.linkup.R;
import com.znv.linkup.ViewSettings;
import com.znv.linkup.WelcomeActivity;
import com.znv.linkup.core.config.LevelCfg;
import com.znv.linkup.db.DbScore;
import com.znv.linkup.db.LevelScore;
import com.znv.linkup.rest.IUpload;
import com.znv.linkup.rest.LevelInfo;
import com.znv.linkup.rest.UserInfo;
import com.znv.linkup.rest.UserScore;
import com.znv.linkup.util.ShareUtil;
import com.znv.linkup.view.LevelTop;
import com.znv.linkup.view.LevelTop.LevelTopStatus;

/**
 * 游戏结果确认框
 * 
 * @author yzb
 * 
 */
public class SuccessDialog extends Dialog implements IUpload {

    private GameActivity linkup = null;
    private ResultInfo resultInfo = null;
    private LevelTop levelTop = null;
    private ShareUtil shareHelper = null;

    public SuccessDialog(final GameActivity linkup) {
        super(linkup, R.style.CustomDialogStyle);
        this.linkup = linkup;
        setContentView(R.layout.success_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        shareHelper = new ShareUtil(getContext());

        TextView btnCancel = (TextView) findViewById(R.id.success_button_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                levelTop.cancelUrlImages();
                cancel();
                linkup.onBackPressed();
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

        TextView btnShare = (TextView) findViewById(R.id.btnshare);
        btnShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String msg = String.format(getContext().getString(R.string.share_score), linkup.getLevelCfg().getLevelName(),
                        String.valueOf(resultInfo.getScore()));

                // 分享
                if (levelTop.getTopStatus() == LevelTopStatus.TopInfo) {
                    // 带截图分享
                    shareHelper.shareMsgView(msg, levelTop);
                } else {
                    shareHelper.shareMessage(msg);
                }
            }
        });

        TextView btnOk = (TextView) findViewById(R.id.success_button_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                levelTop.cancelUrlImages();
                cancel();
                linkup.next();
            }
        });

        levelTop = (LevelTop) findViewById(R.id.level_top);
        levelTop.setUploadListener(this);
    }

    /**
     * 处理返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            TextView btn = (TextView) findViewById(R.id.success_button_cancel);
            btn.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 显示游戏胜利对话框
     * 
     * @param ResultInfo
     *            游戏结果
     */
    public void showDialog(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
        setGameScore(resultInfo.getScore());
        setGameStar(resultInfo.getStars());
        isNewRecord(resultInfo.isNewRecord());
        if (levelTop != null) {
            levelTop.reset();
        }
        uploadScore();
        show();
    }

    /**
     * 设置游戏总得分
     * 
     * @param score
     *            游戏得分
     */
    private void setGameScore(int score) {
        TextView tvScore = (TextView) findViewById(R.id.success_score);
        tvScore.setText(String.valueOf(score) + getContext().getString(R.string.score_unit));

        TextView tvCoin = (TextView) findViewById(R.id.level_coin_reward);
        tvCoin.setText("+" + String.valueOf(score / 10));
    }

    /**
     * 设置是否是新的记录
     * 
     * @param isNewRecord
     *            是否为新记录
     */
    private void isNewRecord(boolean isNewRecord) {
        ImageView ivRecord = (ImageView) findViewById(R.id.level_champion);
        ivRecord.setVisibility(isNewRecord ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 设置游戏星级
     * 
     * @param star
     *            星级数
     */
    private void setGameStar(int star) {
        ImageView level_star = (ImageView) findViewById(R.id.level_star);
        ImageView star1Reward = (ImageView) findViewById(R.id.level_star1_reward);
        star1Reward.setVisibility(View.GONE);
        ImageView star2Reward = (ImageView) findViewById(R.id.level_star2_reward);
        star2Reward.setVisibility(View.GONE);
        ImageView star3Reward = (ImageView) findViewById(R.id.level_star3_reward);
        star3Reward.setVisibility(View.GONE);
        TextView tvDiamond = (TextView) findViewById(R.id.level_diamond_reward);

        if (star <= 0 || star > 3) {
            return;
        }
        level_star.setImageResource(ViewSettings.StarImages[star - 1]);
        tvDiamond.setText("+" + String.valueOf(star));
        if (star > 0) {
            if (LevelCfg.globalCfg.getPromptNum() < ViewSettings.PromptMaxNum) {
                // promt 增加一次
                LevelCfg.globalCfg.setPromptNum(LevelCfg.globalCfg.getPromptNum() + 1);
            }
            star1Reward.setVisibility(View.VISIBLE);
        }
        if (star > 1) {
            if (LevelCfg.globalCfg.getAddTimeNum() < ViewSettings.AddTimeMaxNum) {
                // AddTime 增加一次
                LevelCfg.globalCfg.setAddTimeNum(LevelCfg.globalCfg.getAddTimeNum() + 1);
            }
            star2Reward.setVisibility(View.VISIBLE);
        }
        if (star > 2) {
            if (LevelCfg.globalCfg.getRefreshNum() < ViewSettings.RefreshMaxNum) {
                // refresh 增加一次
                LevelCfg.globalCfg.setRefreshNum(LevelCfg.globalCfg.getRefreshNum() + 1);
            }
            star3Reward.setVisibility(View.VISIBLE);
        }
        linkup.setGlobalCfg();
    }

    /**
     * 上传分数
     */
    private void uploadScore() {
        // 判断是否已登录
        if (!resultInfo.getUserId().equals("")) {
            LevelInfo scoreInfo = new LevelInfo();
            scoreInfo.setUserId(resultInfo.getUserId());
            scoreInfo.setLevel(resultInfo.getLevel());
            scoreInfo.setDiamond(resultInfo.getStars());
            scoreInfo.setGold(resultInfo.getScore() / 10);

            // 增加奖励的钻石和金币
            WelcomeActivity.userInfo.addDiamond(getContext(), scoreInfo.getDiamond());

            if (resultInfo.isNewRecord()) {
                scoreInfo.setScore(resultInfo.getScore());
                scoreInfo.setTime(resultInfo.getTime());
                WelcomeActivity.userInfo.addGold(getContext(), scoreInfo.getGold());
                UserScore.addGetResult(scoreInfo, levelTop.netMsgHandler);
            } else {
                if (!resultInfo.isUpload()) {
                    scoreInfo.setScore(resultInfo.getMaxScore());
                    scoreInfo.setTime(resultInfo.getMinTime());
                    WelcomeActivity.userInfo.addGold(getContext(), scoreInfo.getGold());
                    UserScore.addGetResult(scoreInfo, levelTop.netMsgHandler);
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
            uploadScore();
        }
    }

    @Override
    public void onLevelResultAdd(Message msg) {
        // 更新是否已上传
        linkup.getLevelCfg().setUpload(true);
        LevelScore ls = new LevelScore(resultInfo.getLevel());
        ls.setIsUpload(1);
        DbScore.updateUpload(ls);
    }
}
