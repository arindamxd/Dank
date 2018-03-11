package me.saket.dank.ui.preferences.adapter;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import me.saket.dank.utils.Pair;
import me.saket.dank.utils.RecyclerViewArrayAdapter;

public class UserPreferencesAdapter extends RecyclerViewArrayAdapter<UserPreferencesScreenUiModel, RecyclerView.ViewHolder>
    implements Consumer<Pair<List<UserPreferencesScreenUiModel>, DiffUtil.DiffResult>>
{

  private static final UserPreferencesScreenUiModel.Type[] VIEW_TYPES = UserPreferencesScreenUiModel.Type.values();
  private final Map<UserPreferencesScreenUiModel.Type, UserPreferencesScreenUiModel.ChildAdapter> childAdapters;

  @Inject
  public UserPreferencesAdapter(
      UserPreferenceSectionHeader.Adapter headerAdapter,
      UserPreferenceSwitch.Adapter switchAdapter,
      UserPreferenceButton.Adapter buttonAdapter)
  {
    childAdapters = new HashMap<>();
    childAdapters.put(UserPreferencesScreenUiModel.Type.SECTION_HEADER, headerAdapter);
    childAdapters.put(UserPreferencesScreenUiModel.Type.SWITCH, switchAdapter);
    childAdapters.put(UserPreferencesScreenUiModel.Type.BUTTON, buttonAdapter);

    setHasStableIds(true);
  }

  @Override
  public int getItemViewType(int position) {
    return getItem(position).type().ordinal();
  }

  @Override
  public long getItemId(int position) {
    return getItem(position).adapterId();
  }

  @Override
  protected RecyclerView.ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
    return childAdapters.get(VIEW_TYPES[viewType]).onCreateViewHolder(inflater, parent);
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      //noinspection unchecked
      childAdapters.get(VIEW_TYPES[holder.getItemViewType()]).onBindViewHolder(holder, getItem(position), payloads);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    //noinspection unchecked
    childAdapters.get(VIEW_TYPES[holder.getItemViewType()]).onBindViewHolder(holder, getItem(position));
  }

  @Override
  public void accept(Pair<List<UserPreferencesScreenUiModel>, DiffUtil.DiffResult> pair) throws Exception {
    updateData(pair.first());
    pair.second().dispatchUpdatesTo(this);
  }
}
