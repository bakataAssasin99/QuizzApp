package com.example.quizapps;


import android.content.Context;
import android.util.ArrayMap;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.quizapps.Models.CategoryModel;
import com.example.quizapps.Models.ProfileModel;
import com.example.quizapps.Models.QuestionModel;
import com.example.quizapps.Models.RankModel;
import com.example.quizapps.Models.TestModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DbQuery {
    public static FirebaseFirestore g_firestore;

    public static List<CategoryModel> g_catList = new ArrayList<>();

    public static int g_selected_cat_index = 0;
    public static List<TestModel> g_testModels = new ArrayList<>();
    public static int g_selected_test_index = 0;

    public static List<String> g_bmIdList=new ArrayList<>();
    public static List<QuestionModel> g_bookmarksList = new ArrayList<>();

    public static List<QuestionModel> g_quesList = new ArrayList<>();
    public static List<QuestionModel> g_quesList_old=new ArrayList<>();

    public static List<RankModel> g_users_list = new ArrayList<>();
    public static int g_usersCount = 0;
    public static boolean isMeOnTopList=false;

    public static final int NOT_VISITED = 0;
    public static final int UNANSWERD = 1;
    public static final int REVIEW = 3;
    public static final int ANSWERD = 2;

    static int tmp;

    public static ProfileModel myprofileModel = new ProfileModel("NA",null,null,0);
    public static RankModel myPerformance = new RankModel(null, 0, -1);

    public static void creaUserData(String emails, String name, MyCompleteListener myCompleteListener){
        Map<String, Object> userData = new ArrayMap<>();

        userData.put("EMAIL_ID", emails);
        userData.put("NAME", name);
        userData.put("TOTAL_SCORE",0);
        userData.put("BOOKMARKS",0);

        DocumentReference userDoc = g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        WriteBatch batch = g_firestore.batch();

        batch.set(userDoc, userData);

        DocumentReference countDoc = g_firestore.collection("USERS").document("TOTAL_USERS");
        batch.update(countDoc, "COUNT", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void saveProfileData(String name, String phone, MyCompleteListener myCompleteListener){
        Map<String, Object> profileData = new ArrayMap<>();
        profileData.put("NAME", name);

        if (phone != null){
            profileData.put("PHONE",phone);
        }

        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .update(profileData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        myprofileModel.setName(name);

                        if (phone != null){
                            myprofileModel.setPhone(phone);
                        }
                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void getUserData(final MyCompleteListener myCompleteListener){
        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                        myprofileModel.setName(documentSnapshot.getString("NAME"));
                        myprofileModel.setEmail(documentSnapshot.getString("EMAIL_ID"));

                        if (documentSnapshot.getString("PHONE") != null)
                            myprofileModel.setPhone(documentSnapshot.getString("PHONE"));

                        if (documentSnapshot.getLong("BOOKMARKS") != null)
                            myprofileModel.setBookmarsCount(documentSnapshot.getLong("BOOKMARKS").intValue());

                        myPerformance.setScore(documentSnapshot.getLong("TOTAL_SCORE").intValue());
                        myPerformance.setName(documentSnapshot.getString("NAME"));

                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void loadMyScore(MyCompleteListener myCompleteListener){

        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("MY_SCORES")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                        for (int i=0;i<g_testModels.size();i++){
                            int top =0;
                            if (documentSnapshot.get(g_testModels.get(i).getTestID()) != null){
                                top = documentSnapshot.getLong(g_testModels.get(i).getTestID()).intValue();
                            }

                            g_testModels.get(i).setTopScore(top);
                        }
                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void loadBmIds(MyCompleteListener myCompleteListener){
        g_bmIdList.clear();

        g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("BOOKMARKS")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                        int count = myprofileModel.getBookmarsCount();
                        for (int i=0;i<count;i++){

                            String bmID = documentSnapshot.getString("BM"+String.valueOf(i+i)+"_ID");
                            g_bmIdList.add(bmID);
                        }
                            myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                            myCompleteListener.onFailure();
                    }
                });
    }


    public static void loadBookmarks(Context context,MyCompleteListener completeListener){
        g_bookmarksList.clear();

        if (g_bmIdList.size()==0)
        {
            Toast.makeText(context, "Bạn chưa thêm câu hỏi nào", Toast.LENGTH_SHORT).show();
            completeListener.onSuccess();
        }

         tmp=0;

        for (int i=0;i<g_bmIdList.size();i++){
            String docID = g_bmIdList.get(i);

            g_firestore.collection("Questions").document(docID)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                            if(documentSnapshot.exists()){
                                g_bookmarksList.add(new QuestionModel(
                                        documentSnapshot.getId(),
                                        documentSnapshot.getString("QUESTIONS"),
                                        documentSnapshot.getString("A"),
                                        documentSnapshot.getString("B"),
                                        documentSnapshot.getString("C"),
                                        documentSnapshot.getString("D"),
                                        documentSnapshot.getLong("ANSWER").intValue(),
                                        0,
                                        -1,false
                                ));

                            }

                            tmp++;
                            if (tmp==g_bmIdList.size()){
                                completeListener.onSuccess();
                            }


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            completeListener.onFailure();
                        }
                    });



        }
    }

    public static void getTopUsers (MyCompleteListener myCompleteListener){
        g_users_list.clear();

        String myUID = FirebaseAuth.getInstance().getUid();

        g_firestore.collection("USERS")
                .whereGreaterThan("TOTAL_SCORE",0)
                .orderBy("TOTAL_SCORE", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {
                        int rank=1;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots){

                            g_users_list.add(new RankModel(
                                    doc.getString("NAME"),
                                    doc.getLong("TOTAL_SCORE").intValue(),
                                    rank
                            ));

                            if (myUID.compareTo(doc.getId()) == 0){
                                isMeOnTopList = true;
                                myPerformance.setRank(rank);
                            }


                            rank++;
                        }

                        myCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void getUsersCount(MyCompleteListener myCompleteListener){
        g_firestore.collection("USERS").document("TOTAL_USERS")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        g_usersCount = documentSnapshot.getLong("COUNT").intValue();

                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void saveResult(int score, MyCompleteListener myCompleteListener){
        WriteBatch batch = g_firestore.batch();

        //Bookmarks
        Map<String,Object> bmData=new ArrayMap<>();
        for (int i=0;i<g_bmIdList.size();i++){
            bmData.put("BM"+String.valueOf(i+1)+"_ID",g_bmIdList.get(i));
        }

        DocumentReference bmDoc=g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid())
                .collection("USER_DATA").document("BOOKMARKS");

        batch.set(bmDoc,bmData);

        DocumentReference userDoc = g_firestore.collection("USERS").document(FirebaseAuth.getInstance().getUid());

        Map<String,Object> userData = new ArrayMap<>();

        userData.put("TOTAL_SCORE",score);
        userData.put("BOOKMARKS",g_bmIdList.size());


        batch.update(userDoc, "TOTAL_SCORE", score);


        if (score > g_testModels.get(g_selected_test_index).getTopScore()){
            DocumentReference scoreDOc = userDoc.collection("USER_DATA").document("MY_SCORES");

            Map<String, Object> testData = new ArrayMap<>();
            testData.put(g_testModels.get(g_selected_test_index).getTestID(), score);
            batch.set(scoreDOc, testData, SetOptions.merge());

        }
        batch.commit()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {

                        if (score > g_testModels.get(g_selected_test_index).getTopScore())
                            // xét top score cho test tương ứng
                            g_testModels.get(g_selected_test_index).setTopScore(score);


                            myPerformance.setScore(score);
                            myCompleteListener.onSuccess();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void loadCategories(final MyCompleteListener  myCompleteListener){
        g_catList.clear();

        g_firestore.collection("QUIZ").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            docList.put(doc.getId(), doc);
                        }
                        QueryDocumentSnapshot catListDoc = docList.get("Categorories");

                        long catCount = catListDoc.getLong("COUNT");

                        for (int i=1;i<=catCount;i++){
                            String catID = catListDoc.getString("CAT" + String.valueOf(i) +"_ID");

                            QueryDocumentSnapshot catDoc = docList.get(catID);

                            int noOfTestGet = catDoc.getLong("NO_OF_TEST").intValue();


                            String catName = catDoc.getString("NAME");

                            g_catList.add(new CategoryModel(catID,catName,noOfTestGet));
                        }

                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myCompleteListener.onFailure();
                    }
                });

    }

    public static void loadQuestion(final MyCompleteListener myCompleteListener){

        g_quesList_old.clear();
        g_quesList.clear();


        g_firestore.collection("Questions")
                .whereEqualTo("CATEGORY", g_catList.get(g_selected_cat_index).getDocID())
                .whereEqualTo("TEST",g_testModels.get(g_selected_test_index).getTestID())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot doc : queryDocumentSnapshots){

                            boolean isBookmared=false;

                            if (g_bmIdList.contains(doc.getId()))
                                isBookmared=true;

                            g_quesList_old.add(new QuestionModel(
                                    doc.getId(),
                                    doc.getString("QUESTION"),
                                    doc.getString("A"),
                                    doc.getString("B"),
                                    doc.getString("C"),
                                    doc.getString("D"),
                                    doc.getLong("ANSWER").intValue(),
                                    -1,
                                    NOT_VISITED,
                                    isBookmared
                            ));
                        }
                        if(g_quesList_old.size()>=20){
                            int count=1;
                            while (count<=20){
                                Random random = new Random();
                                int randomques = random.nextInt(g_quesList_old.size()-1);
                                if (!g_quesList.contains(g_quesList_old.get(randomques))){
                                    g_quesList.add(g_quesList_old.get(randomques));
                                    count++;
                                }
                            }
                        }



                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        myCompleteListener.onFailure();
                    }
                });
    }

    public static void loadTestData(final MyCompleteListener myCompleteListener){
        //if (g_testModels.size()>0){
            g_testModels.clear();
        //}


        // check ng dùng chọn categories nào
        g_firestore.collection("QUIZ").document(g_catList.get(g_selected_cat_index).getDocID())
                .collection("TESTS_LIST").document("TESTS_INFO")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {

                        int noOfTest = g_catList.get(g_selected_cat_index).getNoOfTest();
                        for (int i=1; i<=noOfTest;i++){

                            g_testModels.add(new TestModel(
                                    documentSnapshot.getString("TEST" + String.valueOf(i) + "_ID"),
                                    0,
                                    documentSnapshot.getLong("TEST" + String.valueOf(i) + "_TIME").intValue()
                            ));
                        }

                        myCompleteListener.onSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        myCompleteListener.onFailure();
                    }
                });

    }

    public static void loadData(MyCompleteListener completeListener){
        loadCategories(new MyCompleteListener() {
            @Override
            public void onSuccess() {

                getUserData(new MyCompleteListener() {
                    @Override
                    public void onSuccess() {
                        getUsersCount(new MyCompleteListener() {
                            @Override
                            public void onSuccess() {
                                loadBmIds(completeListener);
                            }

                            @Override
                            public void onFailure() {
                                completeListener.onFailure();
                            }
                        });
                    }

                    @Override
                    public void onFailure() {
                        completeListener.onFailure();
                    }
                });
            }

            @Override
            public void onFailure() {
                completeListener.onFailure();
            }
        });

    }


}
