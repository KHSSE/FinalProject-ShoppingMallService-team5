package com.hifi.hifi_shopping.buy.fragment

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.hifi.hifi_shopping.R
import com.hifi.hifi_shopping.auth.AuthActivity
import com.hifi.hifi_shopping.buy.BuyActivity
import com.hifi.hifi_shopping.buy.buy_vm.OrderItemViewModel
import com.hifi.hifi_shopping.buy.buy_vm.OrderProduct
import com.hifi.hifi_shopping.buy.buy_vm.OrderUserViewModel
import com.hifi.hifi_shopping.databinding.FragmentOrderBinding
import com.hifi.hifi_shopping.databinding.RowOrderItemListBinding
import kotlin.concurrent.thread


class OrderFragment : Fragment() {

    private lateinit var fragmentOrderBinding: FragmentOrderBinding
    private lateinit var buyActivity: BuyActivity

    private lateinit var orderUserViewModel: OrderUserViewModel
    private lateinit var orderItemViewModel: OrderItemViewModel
    private var orderUserIdx = ""
    private var selAddressIdx = 0
    private lateinit var orderItemList : ArrayList<String>
    private var orderProductList = mutableListOf<OrderProduct>()
    private lateinit var rowOrderItemListBinding: RowOrderItemListBinding

    private var totalOrderProductCount = 0
    private var totalOrderProductPrice = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        dataSetting()
        viewModelSetting()
        clickEventSetting()

        return fragmentOrderBinding.root
    }


    override fun onResume() {
        super.onResume()
        orderProductList.clear()
    }

    private fun dataSetting(){
        fragmentOrderBinding = FragmentOrderBinding.inflate(layoutInflater)
        buyActivity = activity as BuyActivity
        orderUserIdx = arguments?.getString("userIdx")!!
        orderItemList = arguments?.getStringArrayList("selProduct")!!
        fragmentOrderBinding.orderPayBtnCount.text = "Total ${orderProductList.size} Items"
    }

    private fun clickEventSetting(){
        fragmentOrderBinding.run{

            orderUserBtnToggle.run{
                setOnClickListener {
                    orderUserLayoutAuthComplete.isVisible = !orderUserLayoutAuthComplete.isVisible
                    if (orderUserLayoutAuthComplete.isVisible) {
                        setImageResource(R.drawable.expand_less_24px)
                    } else {
                        setImageResource(R.drawable.expand_more_24px)
                    }
                }
            }

            fragmentOrderBinding.orderUserBtnAuth.setOnClickListener {
                fragmentOrderBinding.orderUserLayoutAuthComplete.visibility = View.VISIBLE
            }

            orderUserAuthCommit.setOnClickListener {
                if(orderUserViewModel.verify.value!!) return@setOnClickListener
                orderUserViewModel.setOrderUserAuth(orderUserIdx)
            }

            toolbarOrder.run{
                setOnMenuItemClickListener {// 임시
                    val intent = Intent(context, AuthActivity::class.java)
                    startActivity(intent)
                    true
                }
                setNavigationOnClickListener {
                    buyActivity.removeFragment(BuyActivity.ORDER_FRAGMENT)
                }
            }

            val selectFont = ResourcesCompat.getFont(buyActivity, R.font.notosanskr_bold)
            val nomalFont = ResourcesCompat.getFont(buyActivity, R.font.notosanskr_medium)
            orderDeliverBtnFirst.run{
                setOnClickListener {
                    setBackgroundResource(R.drawable.background_subscribe_button)
                    typeface = selectFont
                    setTextColor(Color.WHITE)
                    orderDeliverBtnSecond.setBackgroundResource(R.drawable.address_not_select_backround)
                    orderDeliverBtnSecond.setTextColor(buyActivity.getColor(R.color.lstButtonTextGrayColor))
                    orderDeliverBtnSecond.typeface = nomalFont
                    orderDeliverBtnThird.setBackgroundResource(R.drawable.address_not_select_backround)
                    orderDeliverBtnThird.setTextColor(buyActivity.getColor(R.color.lstButtonTextGrayColor))
                    orderDeliverBtnThird.typeface = nomalFont
                    selAddressIdx = 0
                    orderUserViewModel.lookOrderUserAddress(selAddressIdx)
                }
            }

            orderDeliverBtnSecond.run{
                setOnClickListener {
                    setBackgroundResource(R.drawable.background_subscribe_button)
                    typeface = selectFont
                    setTextColor(Color.WHITE)
                    orderDeliverBtnFirst.setBackgroundResource(R.drawable.address_not_select_backround)
                    orderDeliverBtnFirst.setTextColor(buyActivity.getColor(R.color.lstButtonTextGrayColor))
                    orderDeliverBtnFirst.typeface = nomalFont
                    orderDeliverBtnThird.setBackgroundResource(R.drawable.address_not_select_backround)
                    orderDeliverBtnThird.setTextColor(buyActivity.getColor(R.color.lstButtonTextGrayColor))
                    orderDeliverBtnThird.typeface = nomalFont
                    selAddressIdx = 1
                    orderUserViewModel.lookOrderUserAddress(selAddressIdx)
                }
            }

            orderDeliverBtnThird.run{
                setOnClickListener {
                    setBackgroundResource(R.drawable.background_subscribe_button)
                    typeface = selectFont
                    setTextColor(Color.WHITE)
                    orderDeliverBtnFirst.setBackgroundResource(R.drawable.address_not_select_backround)
                    orderDeliverBtnFirst.setTextColor(buyActivity.getColor(R.color.lstButtonTextGrayColor))
                    orderDeliverBtnFirst.typeface = nomalFont
                    orderDeliverBtnSecond.setBackgroundResource(R.drawable.address_not_select_backround)
                    orderDeliverBtnSecond.setTextColor(buyActivity.getColor(R.color.lstButtonTextGrayColor))
                    orderDeliverBtnSecond.typeface = nomalFont
                    selAddressIdx = 2
                    orderUserViewModel.lookOrderUserAddress(selAddressIdx)
                }
            }

            orderDeliverBtnSave.run{
                setOnClickListener {
                    orderUserViewModel.addressData.value?.receiver = orderDeliverEditName.text.toString()
                    orderUserViewModel.addressData.value?.receiverPhoneNum = orderDeliverEditPhone.text.toString()
                    orderUserViewModel.addressData.value?.address = "${orderDeliverEditAddr.text}/${orderDeliverEditAddrDetail.text}"
                    orderUserViewModel.addressData.value?.context = orderDeliverMemoEditText.text.toString()
                    orderUserViewModel.orderUserAddressList.value!![selAddressIdx] =
                        orderUserViewModel.addressData.value!!
                    orderUserViewModel.setOrderUserAddress(selAddressIdx)
                }
            }

            orderDeliverBtnMemoSelect.run{
                setOnClickListener{
                    orderDeliverMemoEditText.isVisible = !orderDeliverMemoEditText.isVisible
                    if(orderDeliverMemoEditText.isVisible){
                        softInputVisible(orderDeliverMemoEditText, true)
                        orderDeliverMemoVisibleBtn.setImageResource(R.drawable.expand_less_24px)
                    } else {
                        softInputVisible(this, false)
                        orderDeliverMemoVisibleBtn.setImageResource(R.drawable.expand_more_24px)
                    }
                }
            }

            orderDeliverMemoVisibleBtn.run{
                setOnClickListener{
                    orderDeliverMemoEditText.isVisible = !orderDeliverMemoEditText.isVisible
                    if(orderDeliverMemoEditText.isVisible){
                        softInputVisible(orderDeliverMemoEditText, true)
                        orderDeliverMemoVisibleBtn.setImageResource(R.drawable.expand_less_24px)
                    } else {
                        softInputVisible(this, false)
                        orderDeliverMemoVisibleBtn.setImageResource(R.drawable.expand_more_24px)
                    }
                }
            }

            orderDeliverBtnToggle.run{
                setOnClickListener {
                    orderDeliverLayout.isVisible = !orderDeliverLayout.isVisible
                    if(orderDeliverLayout.isVisible){
                        this.setImageResource(R.drawable.expand_less_24px)
                    } else {
                        this.setImageResource(R.drawable.expand_more_24px)
                    }
                }
            }

            orderItemListBtnToggle.run{
                setOnClickListener {
                    orderItemListLayout.isVisible = !orderItemListLayout.isVisible
                    if(orderItemListLayout.isVisible){
                        this.setImageResource(R.drawable.expand_less_24px)
                    } else {
                        this.setImageResource(R.drawable.expand_more_24px)
                    }
                }
            }
        }
    }

    private fun softInputVisible(view:View, visible: Boolean){
        if(visible){
            view.requestFocus()
            val inputMethodManger = buyActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            thread {
                SystemClock.sleep(200)
                inputMethodManger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
        }else {
            val inputMethodManager = buyActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            view.clearFocus() // 뷰의 포커스를 해제합니다.
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0) // 키보드를 숨깁니다.
        }

    }

    private fun viewModelSetting(){
        orderUserViewModel = ViewModelProvider(buyActivity)[OrderUserViewModel::class.java]
        orderItemViewModel = ViewModelProvider(buyActivity)[OrderItemViewModel::class.java]

        orderItemViewModel.run{
            productMap.observe(buyActivity){
                totalOrderProductCount = 0
                totalOrderProductPrice = 0
                productViewSetting(it)
            }

            orderItemList.forEach {
                getOrderProductData(it)
            }
        }

        orderUserViewModel.run{
            verify.observe(buyActivity){
                if(it){
                    fragmentOrderBinding.orderUserBtnAuth.visibility = View.GONE
                } else {
                    fragmentOrderBinding.orderUserBtnAuth.visibility = View.VISIBLE
                    fragmentOrderBinding.orderUserLayoutAuthComplete.visibility = View.GONE
                }
            }

            nickname.observe(buyActivity){
                fragmentOrderBinding.orderUserName.text = it
            }

            phoneNum.observe(buyActivity){
                fragmentOrderBinding.orderUserPhoneNumberTextView.text = it
            }

            addressData.observe(buyActivity){
                fragmentOrderBinding.orderDeliverEditName.setText(it.receiver)
                fragmentOrderBinding.orderDeliverEditPhone.setText(it.receiverPhoneNum)
                fragmentOrderBinding.orderDeliverEditAddr.setText(it.address.split("/")[0])
                fragmentOrderBinding.orderDeliverEditAddrDetail.setText(it.address.split("/")[1])
                fragmentOrderBinding.orderDeliverMemoEditText.setText(it.context)
            }

            getOdderUserAddress(orderUserIdx,0)
            orderUserAuthCheck(orderUserIdx)
        }
    }

    private fun productViewSetting(map: LinkedHashMap<String, OrderProduct>){
        orderProductList.clear()
        fragmentOrderBinding.orderItemListLayout.removeAllViews()
        for(itemIdx in orderItemList){
            if(map[itemIdx] != null) {
                orderProductList.add(map[itemIdx]!!)
                getTotalCount(1, true)
                getTotalPrice(map[itemIdx]!!.price, true )

                rowOrderItemListBinding = RowOrderItemListBinding.inflate(layoutInflater)
                rowOrderItemListBinding.run{
                    rowOrderItemListName.text = map[itemIdx]!!.name
                    rowOrderItemListPrice.text = map[itemIdx]!!.price
                    if(map[itemIdx]!!.img != null){
                        rowOrderItemListImg.setImageBitmap(map[itemIdx]!!.img)
                    }
                    rowOrderItemListBtnPlus.setOnClickListener {
                        var oriCount = rowOrderItemListCount.text.toString().toInt()
                        oriCount++
                        rowOrderItemListCount.text = oriCount.toString()
                        getTotalPrice(rowOrderItemListPrice.text.toString(), true)
                        getTotalCount(1, true)
                    }
                    rowOrderItemListBtnMinus.setOnClickListener {
                        var oriCount = rowOrderItemListCount.text.toString().toInt()
                        if(oriCount > 1) {
                            oriCount--
                            getTotalPrice(rowOrderItemListPrice.text.toString(), false)
                            getTotalCount(1, false)
                        }
                        rowOrderItemListCount.text = oriCount.toString()
                    }
                }
                fragmentOrderBinding.orderItemListLayout.addView(rowOrderItemListBinding.root)
                fragmentOrderBinding.orderPayBtnCount.text = "Total $totalOrderProductCount Items"
            }
        }
    }

    fun getTotalPrice(Price: String, plus: Boolean){
        val sb = StringBuilder()
        val sumPrice = if(plus) totalOrderProductPrice + Price.toInt() else totalOrderProductPrice - Price.toInt()
        totalOrderProductPrice = sumPrice
        if(sumPrice < 0) return
        sumPrice.toString().reversed().forEachIndexed { index, c ->
            sb.append("$c")
            if((index+1) % 3 == 0)sb.append(",")
        }
        if(sb.last() == ',') sb.deleteCharAt(sb.lastIndex)
        fragmentOrderBinding.orderPayBtnTotal.text = "${sb.reverse()}원"
    }

    fun getTotalCount(num: Int, plus: Boolean){
        totalOrderProductCount = if(plus) totalOrderProductCount + num else totalOrderProductCount - num
        fragmentOrderBinding.orderPayBtnCount.text = "Total $totalOrderProductCount Items"
    }
}