package com.znv.linkup.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.znv.linkup.BaseActivity;
import com.znv.linkup.R;
import com.znv.linkup.ViewSettings;
import com.znv.linkup.core.Game;
import com.znv.linkup.core.GameSettings;
import com.znv.linkup.core.card.Piece;
import com.znv.linkup.core.card.PiecePair;
import com.znv.linkup.core.util.ImageUtil;

/**
 * 管理界面游戏卡片集合
 * 
 * @author yzb
 * 
 */
public class CardsView extends RelativeLayout {

    public CardsView(Context context) {
        super(context);
    }

    public CardsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGame(Game game) {
        this.game = game;

        // 加载界面图片
        // String skinName = game.getLevelCfg().getGameSkin();
        // loadImages(skinName);

        // 生成游戏卡片
        createCards(false);
    }

    /**
     * 选择卡片
     * 
     * @param piece
     */
    public void check(Piece piece) {
        gameCards[piece.getIndexY()][piece.getIndexX()].setChecked(true);
    }

    /**
     * 取消选择卡片
     * 
     * @param piece
     */
    public void unCheck(Piece piece) {
        gameCards[piece.getIndexY()][piece.getIndexX()].setChecked(false);
    }

    /**
     * 提示卡片对
     * 
     * @param pair
     */
    public void prompt(PiecePair pair) {
        gameCards[pair.getPieceOne().getIndexY()][pair.getPieceOne().getIndexX()].prompt();
        gameCards[pair.getPieceTwo().getIndexY()][pair.getPieceTwo().getIndexX()].prompt();
    }

    /**
     * 取消提示卡片对
     * 
     * @param pair
     */
    public void unPrompt(PiecePair pair) {
        gameCards[pair.getPieceOne().getIndexY()][pair.getPieceOne().getIndexX()].unPrompt();
        gameCards[pair.getPieceTwo().getIndexY()][pair.getPieceTwo().getIndexX()].unPrompt();
    }

    /**
     * 移除卡片
     * 
     * @param piece
     */
    public void erase(Piece piece) {
        removeView(gameCards[piece.getIndexY()][piece.getIndexX()]);
        gameCards[piece.getIndexY()][piece.getIndexX()] = null;
    }

    /**
     * 生成游戏卡片
     * 
     * @param isAnim
     *            是否应用动画
     */
    public void createCards(boolean isAnim) {
        String skinName = game.getLevelCfg().getGameSkin();
        List<Bitmap> scaleBitmaps = loadImages(skinName);
        removeAllViews();
        gameCards = new GameCard[game.getPieces().length][game.getPieces()[0].length];
        GameCard card = null;
        for (int i = 0; i < game.getPieces().length; i++) {
            for (int j = 0; j < game.getPieces()[i].length; j++) {
                Piece p = game.getPieces()[i][j];

                if (p.getImageId() != GameSettings.GroundCardValue) {
                    Bitmap pbm = null;
                    if (p.getImageId() == GameSettings.ObstacleCardValue) {
                        pbm = ImageUtil.scaleBitmap(obstacleBitmap, p.getWidth(), p.getHeight());
                    } else {
                        pbm = scaleBitmaps.get(p.getImageId() - 1);
                    }
                    card = new GameCard(getContext());
                    // 需要先addView，再设置Piece
                    addView(card, p.getWidth(), p.getHeight());

                    card.setPiece(p, pbm, isAnim);
                    card.setOnClickListener(cardClickHandler);

                    gameCards[i][j] = card;
                } else {
                    gameCards[i][j] = null;
                }
            }
        }
    }

    private final OnClickListener cardClickHandler = new OnClickListener() {
        public void onClick(View v) {
            game.click(((GameCard) v).getPiece());
        }
    };

    /**
     * 根据皮肤加载图片，并加入缓存
     * 
     * @param skinName
     *            皮肤名称
     * @return 图片列表
     */
    private List<Bitmap> getSkinImages(String skinName) {
        int index = 0;
        for (int i = 0; i < ViewSettings.SkinNames.length; i++) {
            if (ViewSettings.SkinNames[i].equalsIgnoreCase(skinName)) {
                index = i;
                break;
            }
        }
        while (BaseActivity.skinImages.get(index) == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return BaseActivity.skinImages.get(index);
    }

    /**
     * 根据皮肤加载图片
     */
    private List<Bitmap> loadImages(String skinName) {

        int scaleWidth = game.getPieces()[0][0].getWidth();
        int scaleHeight = game.getPieces()[0][0].getHeight();
        String scaleKey = String.format("%s_%s_%s", skinName, String.valueOf(scaleWidth), String.valueOf(scaleHeight));
        if (!BaseActivity.scaleImages.containsKey(scaleKey)) {
            // 根据皮肤获取图片列表
            List<Bitmap> images = getSkinImages(skinName);

            // 加载到缓存
            List<Bitmap> scaleBms = new ArrayList<Bitmap>();
            for (Bitmap bm : images) {
                scaleBms.add(ImageUtil.scaleBitmap(bm, scaleWidth, scaleHeight));
            }
            BaseActivity.scaleImages.put(scaleKey, scaleBms);
        }
        return BaseActivity.scaleImages.get(scaleKey);

        // Piece[][] pieces = game.getPieces();
        // for (int i = 0; i < pieces.length; i++) {
        // for (int j = 0; j < pieces[i].length; j++) {
        // Piece piece = pieces[i][j];
        // if (piece != null) {
        // // 设置游戏卡片和障碍卡片
        // if (piece.getImageId() == GameSettings.ObstacleCardValue) {
        // Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle);
        // piece.setImage(ImageUtil.scaleBitmap(bm, piece.getWidth(), piece.getHeight()));
        // } else if (piece.getImageId() != GameSettings.GroundCardValue) {
        // // 根据需要缩放图片
        // piece.setImage(ImageUtil.scaleBitmap(images.get(piece.getImageId() - 1), piece.getWidth(), piece.getHeight()));
        // }
        // }
        // }
        // }
    }

    private Game game;
    private GameCard[][] gameCards = null;
    private Bitmap obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle);
}
