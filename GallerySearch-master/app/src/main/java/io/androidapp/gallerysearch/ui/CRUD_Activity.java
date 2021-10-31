package io.androidapp.gallerysearch.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import io.androidapp.gallerysearch.R;
import io.androidapp.gallerysearch.databinding.TagCrudBinding;
import io.androidapp.gallerysearch.model.Keyword;
import io.androidapp.gallerysearch.model.Photo;
import io.androidapp.gallerysearch.model.local.AppDatabase;
import io.androidapp.gallerysearch.utils.KtUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class CRUD_Activity extends Activity {
    Photo photo;
    Photo photoDB;
    private AppDatabase db;
    private TagCrudBinding binding;
    private ArrayAdapter adapter;
    private List<String> tags;
    private ImageButton[] buttons;
    private ListView listview;
    private String tagStr;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = TagCrudBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(getApplication().getApplicationContext());

        photo = getIntent().getParcelableExtra("photo");

        tags = new ArrayList<>();

        buttons = new ImageButton[7];
        int[] btnId = {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7,};
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = findViewById(btnId[i]);
        }

        Observable.just(db)
                .subscribeOn(Schedulers.io())
                .subscribe (db -> {
                    photoDB = db.photoDao().getPhoto(photo.getPath());
                    try{
                        int i = 0;
                        for (String t: photoDB.getTags()){
                            if(i == 0) {i++; continue;}
                            tags.add(t);
                            Timber.i("db 연결");
                        }
                        for (int j = 0; j < buttons.length; j++) {
                            if (j >= tags.size()) {
                                buttons[j].setVisibility(View.INVISIBLE);
                            }
                        }
                    }catch(NullPointerException e){
                        Timber.i("객체 연결");
                    }
                } );


        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, tags);
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);

        binding.plusTag.setOnClickListener(view -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("태그 추가하기\n");
            alert.setMessage("추가시킬 태그를 입력하세요.");

            final EditText name = new EditText(this);
            alert.setView(name);
            alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    String tagStr = name.getText().toString();
                    tags.add(tagStr);
                    LinkedHashSet<String> tagset = new LinkedHashSet<>(tags);
                    photo.setTags(tagset);
                    Observable.just(db)
                            .subscribeOn(Schedulers.io())
                            .subscribe (db -> {
                                        db.photoDao().insertPhoto(photo);
                                        Keyword keyword = db.keywordDao().getKeyword(tagStr);
                                        if (keyword == null) {
                                            Keyword newKey = new Keyword(tagStr);
                                            newKey.setCount(1);
                                            db.keywordDao().insert(newKey);
                                        } else {
                                            keyword.setCount(keyword.getCount() + 1);
                                            db.keywordDao().insert(keyword);
                                        }
                                    }
                            );
                    //delete버튼 추가
                    int tagNum = tags.size();
                    buttons[tagNum-1].setVisibility(View.VISIBLE);
                }
            });
            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            alert.show();
            //showKeyboard();
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EditText name = new EditText(CRUD_Activity.this);
                name.setText(tags.get(position));
                AlertDialog.Builder alert = new AlertDialog.Builder(CRUD_Activity.this);
                alert.setView(name);
                alert.setTitle("태그 수정하기\n");
                alert.setMessage("수정할 태그를 입력하세요.");

                alert.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newTag = name.getText().toString();
                        String oldTag = tags.get(position);
                        tags.set(position, newTag);
                        LinkedHashSet<String> tagset = new LinkedHashSet<>(tags);
                        photo.setTags(tagset);
                        Observable.just(db)
                                .subscribeOn(Schedulers.io())
                                .subscribe (db -> {
                                            db.photoDao().insertPhoto(photo);

                                            Keyword oldkeyword = db.keywordDao().getKeyword(oldTag);
                                            oldkeyword.setCount(oldkeyword.getCount()-1);
                                            db.keywordDao().insert(oldkeyword);
                                            db.keywordDao().deleteZeroCount();

                                            Keyword keyword = db.keywordDao().getKeyword(newTag);
                                            if (keyword == null) {
                                                Keyword newKey = new Keyword(newTag);
                                                newKey.setCount(1);
                                                db.keywordDao().insert(newKey);
                                            } else {
                                                keyword.setCount(keyword.getCount() + 1);
                                                db.keywordDao().insert(keyword);
                                            }
                                        }
                                );
                    }
                });
                alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alert.show();
            }
        });

        ImageButton.OnClickListener onClickListener = new ImageButton.OnClickListener(){

            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.button1:
                        tagStr = tags.get(0);
                        set_alert();
                        break;
                    case R.id.button2:
                        tagStr = tags.get(1);
                        set_alert();
                        break;
                    case R.id.button3:
                        tagStr = tags.get(2);
                        set_alert();
                        break;
                    case R.id.button4:
                        tagStr = tags.get(3);
                        set_alert();
                        break;
                    case R.id.button5:
                        tagStr = tags.get(4);
                        set_alert();
                        break;
                    case R.id.button6:
                        tagStr = tags.get(5);
                        set_alert();
                        break;
                    case R.id.button7:
                        tagStr = tags.get(6);
                        set_alert();
                        break;
                }
            }

        };

        binding.button1.setOnClickListener(onClickListener);
        binding.button2.setOnClickListener(onClickListener);
        binding.button3.setOnClickListener(onClickListener);
        binding.button4.setOnClickListener(onClickListener);
        binding.button5.setOnClickListener(onClickListener);
        binding.button6.setOnClickListener(onClickListener);
        binding.button7.setOnClickListener(onClickListener);
    }

    public void set_alert(){
        AlertDialog.Builder alert = new AlertDialog.Builder(CRUD_Activity.this);
        alert.setTitle("태그 삭제\n");
        alert.setMessage("태그를 삭제하시겠습니까?");
        alert.setPositiveButton("확인",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                tags.remove(tagStr);
                listview.invalidateViews();
                LinkedHashSet<String> tagset = new LinkedHashSet<>(tags);
                photo.setTags(tagset);
                Observable.just(db)
                        .subscribeOn(Schedulers.io())
                        .subscribe (db -> {
                                    db.photoDao().insertPhoto(photo);
                                    Keyword keyword = db.keywordDao().getKeyword(tagStr);
                                    keyword.setCount(keyword.getCount()-1);
                                    db.keywordDao().insert(keyword);
                                    db.keywordDao().deleteZeroCount();
                                }
                        );
                int tagNum = tags.size();
                buttons[tagNum].setVisibility(View.INVISIBLE);
            }
        });
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alert.show();
    }
}