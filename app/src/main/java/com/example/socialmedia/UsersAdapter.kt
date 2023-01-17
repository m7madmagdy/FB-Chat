package com.example.socialmedia

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.socialmedia.data.User
import com.example.socialmedia.databinding.UserCardBinding

class UsersAdapter(
    private val onItemClicked: (v: View, position: Int) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    var usersList: ArrayList<User?> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view = UserCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return UsersViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = usersList[position]
        holder.setUserView(user!!)
    }

    override fun getItemCount() = usersList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(users: ArrayList<User?>) {
        usersList = users
        notifyDataSetChanged()
    }

    fun getUser(position: Int): User? {
        return usersList[position]
    }

    inner class UsersViewHolder(
        val binding: UserCardBinding,
        private val onItemClicked: (v: View, position: Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.userLayout.setOnClickListener(this)
        }

        fun setUserView(user: User) {
            binding.apply {
                Glide.with(root.context)
                    .load(user.cover)
                    .into(coverPhoto)

                Glide.with(root.context)
                    .load(user.avatar)
                    .into(avatarImage)

                userName.text = user.name
                userEmail.text = user.email
                userPhone.text = user.phone
            }

            binding.apply {
                if (binding.userEmail.text != root.context.getString(R.string.admin_email)) {
                    binding.userName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
            }
        }

        override fun onClick(v: View) {
            onItemClicked(v, adapterPosition)
        }
    }
}