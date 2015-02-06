AnimatorSet在PlayTogether的时候，所包含Animator中的第一个如果是startDelay，那么这个delay将有可能不起作用，仅仅指第一个。
selector等资源中不能使用?attr/xxx
长按复制文字时，复制ActionBar不下推view，在Style里添加<item name="windowActionModeOverlay">true</item>
如果MainActivity也使用BlankTheme，会出现蛋疼的问题，底部的Activity必须不能是透明的，但是这样的话StartActivity时的R.anim.slide_out_left和finish时的R.anim.slide_in_left就没法用了，原因不详……
ImageView使用的bitmap不能超过4096x4096……

直接设置ActionBar的分享按钮Provider不起作用，要在onCreateOptionsMenu中这样

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(net.nashlegend.sourcewall.R.menu.menu_article, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(item,mShareActionProvider);
        return true;
    }

    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "222");
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }