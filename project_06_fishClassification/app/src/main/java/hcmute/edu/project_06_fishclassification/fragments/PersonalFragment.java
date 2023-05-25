package hcmute.edu.project_06_fishclassification.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.project_06_fishclassification.ChangePassActivity;
import hcmute.edu.project_06_fishclassification.LoginActivity;
import hcmute.edu.project_06_fishclassification.R;
import hcmute.edu.project_06_fishclassification.adpater.ImageItemAdapter;
import hcmute.edu.project_06_fishclassification.model.Image;
import hcmute.edu.project_06_fishclassification.model.User;

public class PersonalFragment extends Fragment implements View.OnClickListener, Toolbar.OnMenuItemClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_personal, container, false);
    }
    TextView userName, mCollection;
    private RecyclerView rclView;
    private List<Image> savedImages;
    private ImageItemAdapter imageItemAdapter;
    FirebaseFirestore fStore;
    ProgressBar progressBar;
    FirebaseAuth auth;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = getView().findViewById(R.id.topAppBar);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        toolbar.setOnMenuItemClickListener(this);
        auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        savedImages = new ArrayList<>();
        rclView = view.findViewById(R.id.rcl_savedImage);
        userName = view.findViewById(R.id.tv_username);
        mCollection = view.findViewById(R.id.mCollection);
        getUserName();

        //display saved images
        GridLayoutManager gridLayoutManager = new GridLayoutManager(view.getContext(), 2);
        rclView.setLayoutManager(gridLayoutManager);
        imageItemAdapter = new ImageItemAdapter(this.getContext(), savedImages); ///
        rclView.setAdapter(imageItemAdapter);
        GetImageWithUid();

    }

    private void getUserName() {
        fStore.collection("users").document(auth.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.e("Error", "Listen failed.", error);
                            return;
                        }

                        if (value != null && value.exists()) {
                            User user = value.toObject(User.class);
                            userName.setText(user.getName());
                        } else {
                            Log.e("Error", "Current data: null");
                        }
                    }
                });
    }

    private void GetImageWithUid() {
        CollectionReference ref = fStore.collection("saved_images");
        Query query = ref.whereEqualTo("userId",auth.getUid());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    if (progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);
                    Log.e("FireStore error", error.getMessage());
                    return;
                }
                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        Image image = dc.getDocument().toObject(Image.class);
                        String s = mCollection.getText().toString().trim();
                        s = s.substring(s.indexOf("(") + 1, s.indexOf(")"));
                        s = String.valueOf(Integer.valueOf(s) + 1);
                        mCollection.setText("My Collection " + "(" + s + ")");
                        savedImages.add(image);
                    }
                }
                imageItemAdapter.notifyDataSetChanged();
                if (progressBar.getVisibility() == View.VISIBLE)
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                return true;
            case R.id.changePassword:
                Intent intent2 = new Intent(getActivity().getApplicationContext(), ChangePassActivity.class);
                startActivity(intent2);
        }
        return false;
    }

    @Override
    public void onClick(View view) {

    }
}