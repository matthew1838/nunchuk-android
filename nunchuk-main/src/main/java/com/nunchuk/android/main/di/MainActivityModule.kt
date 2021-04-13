package com.nunchuk.android.main.di

import com.nunchuk.android.main.MainActivity
import com.nunchuk.android.main.components.tabs.account.AccountFragment
import com.nunchuk.android.main.components.tabs.account.AccountModule
import com.nunchuk.android.main.components.tabs.message.MessagesFragment
import com.nunchuk.android.main.components.tabs.message.MessagesModule
import com.nunchuk.android.main.components.tabs.wallet.WalletsFragment
import com.nunchuk.android.main.components.tabs.wallet.WalletsModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal interface MainActivityModule {

    @ContributesAndroidInjector(modules = [MainFragmentModule::class])
    fun mainActivity(): MainActivity

}

@Module
internal interface MainFragmentModule {

    @ContributesAndroidInjector(modules = [MessagesModule::class])
    fun messagesFragment(): MessagesFragment

    @ContributesAndroidInjector(modules = [WalletsModule::class])
    fun walletsFragment(): WalletsFragment

    @ContributesAndroidInjector(modules = [AccountModule::class])
    fun accountFragment(): AccountFragment

}