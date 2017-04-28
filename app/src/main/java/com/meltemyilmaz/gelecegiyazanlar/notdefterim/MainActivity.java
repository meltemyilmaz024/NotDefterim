package com.meltemyilmaz.gelecegiyazanlar.notdefterim;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText myNewNot;
    private Button btn_yeniEkle,btn_listele;
    private TextView gor;
    private ListView myNoteList ;


    FirebaseDatabase db;
    DatabaseReference mDatabase;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser user ;
    private String userId;

    private ArrayList<String> notes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseDatabase.getInstance();

        //  initialize_database_ref
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //get current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        myNewNot = (EditText) findViewById(R.id.editTextYeniNot);
        btn_yeniEkle = (Button)findViewById(R.id.btn_ekle);
        btn_listele = (Button)findViewById(R.id.btn_listele);
        myNoteList = (ListView) findViewById(R.id.noteListID);

        showListView(false);

        btn_yeniEkle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String myNot = myNewNot.getText().toString().trim();
                showListView(false);
                NotEkle();
                myNewNot.setText("");  // EditText'in içerisini temizledim.
            }
        });

        btn_listele.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //veriAdap.clear();
                showListView(true);
                NotlariListele();
            }
        });
    }

    private void NotYaz(String userId, String name,  String note){

        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("notes").push().getKey();
        Note not = new Note(userId, name, note);
        Map<String, Object> notIcerigi = not.toMap();

        Map<String,Object> childUpdates = new HashMap<>();
        childUpdates.put("/notes/"+key, notIcerigi);
        childUpdates.put("/user-notes/" + userId + "/" + key, notIcerigi);

        mDatabase.updateChildren(childUpdates);
    }

    private void NotEkle(){
        final String notum = myNewNot.getText().toString();

        if(TextUtils.isEmpty(notum)){
            myNewNot.setError("Required");
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Notunuz ekleniyor...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            // User is null, error out
                            Log.e("Log-MainActivity", "User " + userId + " is unexpectedly null");
                            Toast.makeText(MainActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            NotYaz(userId, user.username, notum);
                        }

                        // Enable button so there are no multi-posts
                        setEditingEnabled(true);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Log-MainActivity", "getUser:onCancelled", databaseError.toException());
                        // Enable button so there are no multi-posts
                        setEditingEnabled(true);
                    }
                });

    }

    private void NotlariListele (){

        Toast.makeText(this, "Listing...", Toast.LENGTH_SHORT).show();
        setEditingEnabled(false);

        //  String ArrayList tanımladım.
        notes = new ArrayList<String>();

        //  ArrayAdaptor oluşturdum ve onu "notes" listem ile bağladım.
        final ArrayAdapter<String> veriAdap = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1, notes);

        DatabaseReference okuma = db.getReference().child("user-notes").child(userId);

        okuma.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Iterable<DataSnapshot> keys = dataSnapshot.getChildren();
                for(DataSnapshot key : keys){
                    // Arrayliste notlarımı ekliyorum.
                    notes.add(key.child("note").getValue().toString());
                }
                setEditingEnabled(true);
                // En son, ListView ile adaptörümü bağladım.
                myNoteList.setAdapter(veriAdap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                setEditingEnabled(true);
            }
        });



        myNoteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder diyalogOlusturucu=
                        new AlertDialog.Builder(MainActivity.this);


                diyalogOlusturucu.setMessage(notes.get(position))
                        .setCancelable(false)
                        .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }

                        });

                diyalogOlusturucu.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
       // progressBar.setVisibility(View.GONE);
    }

    private void setEditingEnabled(boolean enabled) {
        myNewNot.setEnabled(enabled);
        if (enabled) {
            btn_yeniEkle.setVisibility(View.VISIBLE);
        } else {
            btn_yeniEkle.setVisibility(View.INVISIBLE);
        }
    }

    private void showListView (boolean enabled){
        if(enabled){
            myNoteList.setVisibility(View.VISIBLE);
        }else{
            myNoteList.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //Styling for double press back button
    private static long back_pressed;
    @Override
    public void onBackPressed(){
        if (back_pressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            FirebaseAuth.getInstance().signOut();
        }
        else{
            Toast.makeText(getBaseContext(), "Press once again to exit", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }

}
