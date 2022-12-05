package com.example.ratemytoilet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.database.ReviewViewModel
import com.example.ratemytoilet.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withContext
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "ProfileFragment"

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var reviewsStrFormat: String
    private lateinit var levelStrFormat: String
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var adapter: UserCommentListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewsStrFormat = getString(R.string.stat_total_reviews_format)
        levelStrFormat = getString(R.string.stat_level_format)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        if (currentUser != null) {
            adapter = UserCommentListAdapter(requireContext(), profileViewModel.reviews)
            binding.userReviewsListView.adapter = adapter
        } else {
            Log.d("TAp", "null")
        }


        return binding.root
    }

    private fun updateAdapter() {
        Log.i(TAG, "Updating adapter")
        lifecycleScope.launch(Dispatchers.IO) {
            profileViewModel.getReviewsForUserId(currentUser!!.uid)
            withContext(Dispatchers.Main) {
                adapter.update(profileViewModel.reviews)
                adapter.notifyDataSetChanged()
                Log.i(TAG, "adapter has item count: ${adapter.count}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateAdapter()
        updateTotalReviews()
    }

    private fun updateTotalReviews() {
        // Temporary until we get the values
        binding.totalReviewsTextView.text = reviewsStrFormat.format(0)
        binding.levelTextView.text = levelStrFormat.format(0)

        if (currentUser != null) {
            db.document("users/${currentUser!!.uid}").addSnapshotListener { value, error ->
                if (value != null) {
                    val userDocument = value!!.toObject(ToiletUser::class.java)
                    Log.i(TAG, "Got user document: $userDocument")
                    if (userDocument != null) {
                        val reviewCount = userDocument!!.totalReviews ?: 0
                        binding.totalReviewsTextView.text =
                            reviewsStrFormat.format(reviewCount)
                        binding.levelTextView.text = levelStrFormat.format(reviewCount / 5)
                    }
                }
            }
        }
    }

}