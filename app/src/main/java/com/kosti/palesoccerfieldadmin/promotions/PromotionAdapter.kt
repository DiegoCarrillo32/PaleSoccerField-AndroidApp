package com.kosti.palesoccerfieldadmin.promotions

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.kosti.palesoccerfieldadmin.R
import com.kosti.palesoccerfieldadmin.models.PromotionDataModel
import com.kosti.palesoccerfieldadmin.models.ScheduleDataModel
import com.kosti.palesoccerfieldadmin.utils.FirebaseUtils
import com.squareup.picasso.Picasso
import java.util.Date
import java.util.Locale

private val COLLECTION_NAME = "promocion"

class PromotionAdapter(private val dataSet: MutableList<PromotionDataModel>,
                       private val context: Context) : RecyclerView.Adapter<PromotionAdapter.ViewHolder>(), AddEditPromotion.OnDismissListener {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTV : TextView
        val descriptionTV : TextView
        val fechaInicioTV : TextView
        val fechaFinalTV : TextView
        val deleteBtn : ImageButton
        val editBtn : ImageButton
        val image : ImageView
        init {
            nameTV = view.findViewById(R.id.nameTV)
            descriptionTV = view.findViewById(R.id.descriptionTV)
            fechaInicioTV = view.findViewById(R.id.fechaInicio)
            fechaFinalTV = view.findViewById(R.id.fechaFin)
            deleteBtn = view.findViewById(R.id.deleteBtn)
            editBtn = view.findViewById(R.id.editBtn)
            image = view.findViewById(R.id.imageView)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.promotion_row_item, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount() = dataSet.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTV.text = dataSet[position].Name
        holder.descriptionTV.text = dataSet[position].Description
        holder.fechaInicioTV.text = dataSet[position].StartDate.toDate().toString()
        holder.fechaFinalTV.text = dataSet[position].EndDate.toDate().toString()

        val dateInit = Date((dataSet[position].StartDate?.seconds ?: 0) * 1000)
        val dateFormatInit = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDateInit = dateFormatInit.format(dateInit)

        val dateFinal = Date((dataSet[position].EndDate?.seconds ?: 0) * 1000)
        val dateFormatFinal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDateFinal = dateFormatFinal.format(dateFinal)

        holder.fechaInicioTV.text = "Fecha de inicio: $formattedDateInit"
        holder.fechaFinalTV.text = "Fecha de finalizacion: $formattedDateFinal"
        holder.nameTV.text = dataSet[position].Name
        holder.descriptionTV.text = dataSet[position].Description
        // set the image to the imageview using an url
        if(dataSet[position].ImageUrl != "") Picasso.get().load(dataSet[position].ImageUrl).into(holder.image)

        holder.deleteBtn.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setTitle("Eliminar promocion")
            builder.setMessage("¿Estas seguro que deseas eliminar esta promocion?")
            builder.setPositiveButton("Si") { dialog, which ->

                FirebaseUtils().deleteDocument("promocion", dataSet[position].Id)
                dataSet.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, dataSet.size)
            }
            builder.setNeutralButton("Cancelar") {_,_ ->
                //Toast.makeText(context,"You cancelled the dialog.",Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        holder.editBtn.setOnClickListener {
            // open the fragment to edit the promotion data
            val bundle = Bundle()
            val addEditPromotion = AddEditPromotion()
            addEditPromotion.setOnDismissListener(this)

            bundle.putString("id", dataSet[position].Id)
            bundle.putString("name", dataSet[position].Name)
            bundle.putString("description", dataSet[position].Description)
            bundle.putBoolean("status", dataSet[position].Status)
            bundle.putSerializable("startDate", dataSet[position].StartDate.toDate())
            bundle.putSerializable("endDate", dataSet[position].EndDate.toDate())
            bundle.putString("imageUrl", dataSet[position].ImageUrl)
            addEditPromotion.arguments = bundle


            addEditPromotion.show((context as Promotions).supportFragmentManager, "BSDialogFragment")


        }

    }

    override fun onDismissOnActivity() {
        FirebaseUtils().readCollection(COLLECTION_NAME) {
                result ->
            result.onSuccess {
                dataSet.clear()
                for (promotion in it){
                    if(promotion["estado"] != true) continue

                    val promotionData = PromotionDataModel(
                        promotion["id"].toString(),
                        promotion["descripcion"].toString(),
                        promotion["estado"] as Boolean,
                        promotion["fecha_final"] as Timestamp,
                        promotion["fecha_inicio"] as Timestamp,
                        promotion["imagen_url"].toString(),
                        promotion["nombre"].toString(),
                    )
                    dataSet.add(promotionData)

                }
                notifyDataSetChanged()
            }
            result.onFailure {

            }

        }
    }

}