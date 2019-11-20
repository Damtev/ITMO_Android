package com.example.navigation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.navigation_fragment.view.*

class NavigationFragment : Fragment() {

    private var number = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        number = arguments?.getInt("number") ?: 0
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.navigation_fragment, container, false)
        with(rootView) {
            navigation_fragment_text.text = "0" + (1..number).fold("") { acc, i -> "$acc->$i" }
            navigation_fragment_push_button.setOnClickListener {
                requireFragmentManager().apply {
                    beginTransaction()
                        .replace(R.id.tab_fragment_inner_container, NavigationFragment().apply {
                            arguments = Bundle().apply {
                                putInt("number", this@NavigationFragment.number + 1)
                            }
                        })
                        .addToBackStack("")
                        .commit()
                }
            }
        }
        return rootView
    }
}