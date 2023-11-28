package com.kosti.palesoccerfieldadmin.otherUsersProfile

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kosti.palesoccerfieldadmin.R
import com.kosti.palesoccerfieldadmin.blockedUserList.BlockedUsersList
import com.kosti.palesoccerfieldadmin.changePassword.ChangePassword
import com.kosti.palesoccerfieldadmin.reservations.CustomSpinnerAdapter
import com.kosti.palesoccerfieldadmin.userAdminProfile.EditPositionsUser
import com.kosti.palesoccerfieldadmin.userAdminProfile.EditUserData
import com.kosti.palesoccerfieldadmin.utils.FirebaseUtils



private const val ARG_PARAM1 = "name"
private const val ARG_PARAM2 = "age"
private const val ARG_PARAM3 = "phone"
private const val ARG_PARAM4 = "classification"
private const val ARG_PARAM5 = "positions"
private const val ARG_PARAM6 = "nickname"
private const val ARG_PARAM7 = "id"
private const val COLLECTION_NAME = "jugadores"
private const val CLASIFICATION_FIELD = "clasificacion"



class ProfileScreen : BottomSheetDialogFragment() {


    private var name: String? = null
    private var age: Long? = null
    private var phone: String? = null
    private var classification: String? = null
    private var isEditingClassification: Boolean = false
    private var nickname: String? = null
    private var id: String? = null
    private lateinit var positions: MutableList<String>
    private var ratesList = listOf("Malo", "Bueno", "Regular")
    private var onDismissListener: OnDismissListener? = null
    private var didEditClassification: Boolean = false
    private var didEditPositionsUser: Boolean = false
    private lateinit var  bottomSheetBehaviour: BottomSheetBehavior<View>;
    private lateinit var  btnMessage: ImageButton
    private lateinit var viewFragment: View



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_PARAM1)
            age = it.getLong(ARG_PARAM2)
            phone = it.getString(ARG_PARAM3)
            classification = it.getString(ARG_PARAM4)
            positions = it.getStringArrayList(ARG_PARAM5)!!
            nickname = it.getString(ARG_PARAM6)
            id = it.getString(ARG_PARAM7)

        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewFragment = inflater.inflate(R.layout.fragment_profile_screen, container, false)
        spinnerPositions(viewFragment)
        spinnerClassification(viewFragment)
        return viewFragment
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plyrNTV: TextView = view.findViewById(R.id.playerName)
        val ageTV: TextView = view.findViewById(R.id.edadText)
        val phoneTV: TextView = view.findViewById(R.id.telefonoText)
        val classTV: Spinner = view.findViewById(R.id.clasificacionSpinner)

        val nicknameTV: TextView = view.findViewById(R.id.nicknameText)
        val editClasiBtn: ImageButton = view.findViewById(R.id.editClassBtn)
        val rateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, ratesList)
        val closeButton: ImageButton = view.findViewById(R.id.backButton)
        val bloquedPeopleButton: ImageButton = view.findViewById(R.id.bloquedPeopleButton)
        val btnEditPositions:ImageButton = view.findViewById(R.id.edit_position_button)
        btnMessage = view.findViewById(R.id.messageButton)


        //Sheet behaviour
        bottomSheetBehaviour = BottomSheetBehavior.from( view.parent as View);
        //set to behaviour to expanded and minimum height to parent layout
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED

        closeButton.setOnClickListener {
            closeBottomSheet()
        }

        bloquedPeopleButton.setOnClickListener {
            val intent = Intent(context, BlockedUsersList::class.java)
            Log.d("Usercito", "$id")
            intent.putExtra("textParameter", id.toString())
            context?.startActivity(intent)
        }

        rateAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        // The age is a string in a epoch format, so we need to convert it to a date
        // and then calculate the age

        phoneTV.text = phone
        nicknameTV.text = nickname
        val transformedAge = age?.let { FirebaseUtils().transformEpochToAge(it) }
        ageTV.text = transformedAge.toString()
        plyrNTV.text = name

        editClasiBtn.setOnClickListener {
            if (isEditingClassification) {
                classTV.isEnabled = false
                classTV.isClickable = false
                isEditingClassification = false
                if(id != null && classTV.selectedItem.toString().toLowerCase() != classification?.toLowerCase() ) {
                    FirebaseUtils().updateProperty(COLLECTION_NAME,
                        id!!, CLASIFICATION_FIELD, classTV.selectedItem.toString())
                    didEditClassification = true
                }

            } else {
                classTV.isEnabled = true
                classTV.isClickable = true
                isEditingClassification = true

            }
        }

        btnMessage.setOnClickListener {

            val intent = Intent(requireContext(), SendMessage::class.java)
            intent.putExtra("userName", name )
            intent.putExtra("userPhone", phone)
            startActivity(intent)

        }

        btnEditPositions.setOnClickListener { editPositions() }




    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val layoutParams = sheet.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                sheet.layoutParams = layoutParams

                val behavior = BottomSheetBehavior.from(sheet)
                behavior.isHideable = false
                behavior.skipCollapsed = true
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        // No action needed
                    }
                })
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private fun closeBottomSheet() {
        dismiss()
    }


    private fun spinnerPositions(view: View) {
        var elementos = positions

        if(elementos.isEmpty()){
            elementos = mutableListOf("Sin posición")
        }

        val spinner: Spinner = view.findViewById(R.id.spinnerPositions)
        val adapter =
            this.context?.let {
                CustomSpinnerAdapter(
                    it,
                    R.layout.spinner_item_profile,
                    elementos
                )
            }

        adapter?.setDropDownViewResource(R.layout.spinner_item_profile)
        spinner.adapter = adapter

    }

    private fun spinnerClassification(view: View) {
        val elementos = ratesList
        val spinner: Spinner? = view.findViewById(R.id.clasificacionSpinner)
        if(spinner == null){
            Toast.makeText(requireContext(), "Spinner is null", Toast.LENGTH_LONG).show()
            return
        }
        val adapter =
            this.context?.let {
                ArrayAdapter(
                    it,
                    R.layout.spinner_item_clasification,
                    elementos
                )
            }

        adapter?.setDropDownViewResource(R.layout.spinner_item_clasification)
        spinner.adapter = adapter

        spinner.setSelection(ratesList.indexOf(classification), false)
        adapter?.notifyDataSetChanged()
        spinner.isEnabled = false
        spinner.isClickable = false

    }


    fun setOnDismissListener(listener: OnDismissListener) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if(didEditClassification){
            onDismissListener?.onDismissOnActivity()
            didEditClassification = false
        }
        if(didEditPositionsUser){
            onDismissListener?.onDismissOnActivity()
            didEditPositionsUser = false
        }
    }

    private fun activityEditPositions(type: String){
        val intent = Intent(requireContext(), EditPositionsUser::class.java)

        // Agregar datos al Intent
        intent.putStringArrayListExtra("positions", ArrayList(positions))
        intent.putExtra("type", type)
        intent.putExtra("userId", id)
        startActivityForResult(intent, EditUserData.REQUEST_CODE_EDIT_FIELD)
        didEditPositionsUser = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EditUserData.REQUEST_CODE_EDIT_FIELD && resultCode == AppCompatActivity.RESULT_OK) {
            data?.let {
                if (it.hasExtra("posiciones") && it.hasExtra("accion")) {
                    val editedPositions = it.getStringArrayListExtra("posiciones")
                    // Haz algo con las posiciones editadas
                    // Por ejemplo, actualiza la lista en el fragment
                    positions.clear()
                    if (editedPositions != null) {
                        positions.addAll(editedPositions)
                    }
                    spinnerPositions(viewFragment)
                }
            }
        }
    }


    private fun editPositions() {
        activityEditPositions("Posiciones");
    }

    interface OnDismissListener {
        fun onDismissOnActivity()
    }

}


