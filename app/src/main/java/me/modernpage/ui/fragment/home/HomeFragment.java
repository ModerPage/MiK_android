package me.modernpage.ui.fragment.home;


import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */

//@AndroidEntryPoint
//public class HomeFragment extends BaseFragment<HomeViewModel, FragmentHomeBinding> {
//
//
//    public HomeFragment() {
//        // Required empty public constructor
//    }
//
//    @Override
//    public int getLayoutRes() {
//        return R.layout.fragment_home;
//    }
//
//    @Override
//    public Class<HomeViewModel> getViewModel() {
//        return HomeViewModel.class;
//    }
//
//
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        if (getArguments() != null) {
//            String userEmail = getArguments().getString(Profile.class.getSimpleName());
//
//            if(userEmail == null) {
//                throw new IllegalArgumentException("Current user email is not present in the bundle");
//            }
//
//        } else {
//            throw new IllegalArgumentException("Must pass current user email in the bundle.");
//        }
//
//    }
//
//    public static Fragment newInstance(String userEmail) {
//        HomeFragment fragment = new HomeFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(Profile.class.getSimpleName(), userEmail);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//}
