package com.nunchuk.android.main.di

import com.nunchuk.android.main.MainActivity
import com.nunchuk.android.main.components.tabs.chat.ChatFragment
import com.nunchuk.android.main.components.tabs.wallet.WalletsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal interface MainActivityModule {

    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    fun mainActivity(): MainActivity

}

@Module
internal interface MainFragmentModule {

    @ContributesAndroidInjector
    fun chatFragment(): ChatFragment

    @ContributesAndroidInjector
    fun walletsFragment(): WalletsFragment

}
