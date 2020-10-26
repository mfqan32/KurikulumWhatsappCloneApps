package com.mfqan.creating.kurikulumwhatsappclone.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mfqan.creating.kurikulumwhatsappclone.fragments.ChatsFragment
import com.mfqan.creating.kurikulumwhatsappclone.fragments.StatusListFragment
import com.mfqan.creating.kurikulumwhatsappclone.fragments.StatusUpdateFragment

class SectionPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val statusUpdateFragment = StatusUpdateFragment()
    private val chatsFragment = ChatsFragment()
    private val statusFragment = StatusListFragment()

    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> statusUpdateFragment // menempatkan StatusUpdateFragment di posisi pertama
            1 -> chatsFragment
            2 -> statusFragment
            else -> chatsFragment
        }
    }

    override fun getCount(): Int {
        return 3
    }
}