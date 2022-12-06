package com.example.ratemytoilet

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ratemytoilet.database.Review
import com.example.ratemytoilet.databinding.FragmentProfileBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
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
    private lateinit var adapter: FirestoreRecyclerAdapter<Review, ReviewHolder>

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
//            adapter = UserCommentListAdapter(requireContext(), profileViewModel.reviews)
//            binding.userReviewsListView.adapter = adapter

            val query = db
                .collection("users/${currentUser!!.uid}/reviews")
                .orderBy("dateAdded", Query.Direction.DESCENDING)

            val options: FirestoreRecyclerOptions<Review> = FirestoreRecyclerOptions.Builder<Review>()
                .setQuery(query, Review::class.java)
                .setLifecycleOwner(viewLifecycleOwner)
                .build()

            adapter =
                object : FirestoreRecyclerAdapter<Review, ReviewHolder>(options) {
                    override fun onCreateViewHolder(group: ViewGroup, i: Int): ReviewHolder {
                        val view: View = LayoutInflater.from(group.context).inflate(R.layout.cardview_list_layout, group, false)
                        return ReviewHolder(view)
                    }

                    override fun onBindViewHolder(holder: ReviewHolder, position: Int, review: Review) {
                        val dateTimeFormat : DateFormat = SimpleDateFormat ("MMM dd, yyyy")
                        val date = dateTimeFormat.format(review.dateAdded)
                        val userInformation = UserComment(review.id, date, review.cleanliness.toFloat(), review.comment, review.sufficientSoap, review.sufficientPaperTowels, review.accessibility, review.leftByAdmin)

                        holder.dateText.setText(userInformation.date)
                        holder.comment.setText(userInformation.comment)
                        userInformation.paper?.let { setTextState(holder.paperState, it) }
                        userInformation.soap?.let { setTextState(holder.soapState, it) }
                        userInformation.access?.let { setTextState(holder.accState, it) }
                        userInformation.rate?.let { holder.rating.setRating(it) }


                        Log.i(TAG,"Binded viewholder!")
                    }

                    private fun setTextState(textView: TextView, state: Int) {
                        if (state == 1) {
                            textView.setText("Yes")
                            textView.setTextColor(Color.GREEN)
                        } else if (state == 0) {
                            textView.setText("No")
                            textView.setTextColor(Color.RED)
                        } else {
                            textView.setText("Unknown")
                            textView.setTextColor(Color.DKGRAY)
                        }
                    }
                }

            binding.userReviewsRecyclerView.adapter = adapter
            binding.userReviewsRecyclerView.layoutManager = LinearLayoutManager(context)
        }


        return binding.root
    }

//    private fun updateAdapter() {
//        Log.i(TAG, "Updating adapter")
//        lifecycleScope.launch(Dispatchers.IO) {
//            profileViewModel.getReviewsForUserId(currentUser!!.uid)
//            withContext(Dispatchers.Main) {
//                adapter.update(profileViewModel.reviews)
//                adapter.notifyDataSetChanged()
//                Log.i(TAG, "adapter has item count: ${adapter.count}")
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
//        updateAdapter()
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

    class ReviewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView
        val comment: EditText
        val rating: RatingBar
        val paperState: TextView
        val soapState: TextView
        val accState: TextView

        init {
            dateText = view.findViewById<TextView>(R.id.userDate)
            comment = view.findViewById<EditText>(R.id.userComment)
            rating = view.findViewById<RatingBar>(R.id.singleRating)
            paperState = view.findViewById<TextView>(R.id.paperStateText)
            soapState = view.findViewById<TextView>(R.id.soapStateText)
            accState = view.findViewById<TextView>(R.id.accessStateText)
        }
    }

}