import { createTheme } from '@mui/material/styles';

const primaryLight = '#004165';
const onPrimaryLight = '#FFFFFF';
const secondaryLight = '#772432';
const onSecondaryLight = '#FFFFFF';
const errorLight = '#BA1A1A';
const onErrorLight = '#FFFFFF';
const backgroundLight = '#FFF8F7';
const onBackgroundLight = '#22191A';
const surfaceLight = '#FFF8F7';
const onSurfaceLight = '#22191A';

const primaryDark = '#FFB2B9';
const onPrimaryDark = '#561D25';
const secondaryDark = '#E5BDBF';
const onSecondaryDark = '#44292C';
const errorDark = '#FFB4AB';
const onErrorDark = '#690005';
const backgroundDark = '#1A1112';
const onBackgroundDark = '#F0DEDF';
const surfaceDark = '#1E1E1E';
const onSurfaceDark = '#F0DEDF';

export const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: primaryLight,
      contrastText: onPrimaryLight,
    },
    secondary: {
      main: secondaryLight,
      contrastText: onSecondaryLight,
    },
    error: {
      main: errorLight,
      contrastText: onErrorLight,
    },
    background: {
      default: backgroundLight,
      paper: surfaceLight,
    },
    text: {
      primary: onBackgroundLight,
      secondary: onSurfaceLight,
    },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: primaryLight,
          color: onPrimaryLight,
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
        },
        contained: {
          backgroundColor: primaryLight,
          color: onPrimaryLight,
          '&:hover': {
            backgroundColor: '#00304D',
          },
        },
        outlined: {
          borderColor: primaryLight,
          color: primaryLight,
          '&:hover': {
            borderColor: '#00304D',
            backgroundColor: 'rgba(0, 65, 101, 0.04)',
          },
        },
      },
    },
    MuiAccordion: {
      styleOverrides: {
        root: {
          backgroundColor: secondaryLight,
          color: onSecondaryLight,
          '&.Mui-expanded': {
            margin: 0,
          },
        },
      },
    },
    MuiAccordionSummary: {
      styleOverrides: {
        root: {
          backgroundColor: secondaryLight,
          color: onSecondaryLight,
          '&.Mui-expanded': {
            minHeight: 48,
          },
        },
        content: {
          '&.Mui-expanded': {
            margin: '12px 0',
          },
        },
      },
    },
    MuiAccordionDetails: {
      styleOverrides: {
        root: {
          backgroundColor: '#FFFFFF',
          color: onBackgroundLight,
          padding: '16px',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: 'rgba(0, 65, 101, 0.04)',
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        select: {
          backgroundColor: surfaceLight,
          color: onSurfaceLight,
          '&:focus': {
            backgroundColor: surfaceLight,
          },
        },
        icon: {
          color: primaryLight,
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: primaryLight,
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: '#00304D',
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: primaryLight,
          },
        },
      },
    },
  },
});

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: primaryDark,
      contrastText: onPrimaryDark,
    },
    secondary: {
      main: secondaryDark,
      contrastText: onSecondaryDark,
    },
    error: {
      main: errorDark,
      contrastText: onErrorDark,
    },
    background: {
      default: backgroundDark,
      paper: surfaceDark,
    },
    text: {
      primary: onBackgroundDark,
      secondary: onSurfaceDark,
    },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: primaryDark,
          color: onPrimaryDark,
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
        },
        contained: {
          backgroundColor: primaryDark,
          color: onPrimaryDark,
          '&:hover': {
            backgroundColor: '#FF9BA4',
          },
        },
        outlined: {
          borderColor: primaryDark,
          color: primaryDark,
          '&:hover': {
            borderColor: '#FF9BA4',
            backgroundColor: 'rgba(255, 178, 185, 0.08)',
          },
        },
      },
    },
    MuiAccordion: {
      styleOverrides: {
        root: {
          backgroundColor: secondaryDark,
          color: onSecondaryDark,
          '&.Mui-expanded': {
            margin: 0,
          },
        },
      },
    },
    MuiAccordionSummary: {
      styleOverrides: {
        root: {
          backgroundColor: secondaryDark,
          color: onSecondaryDark,
          '&.Mui-expanded': {
            minHeight: 48,
          },
        },
        content: {
          '&.Mui-expanded': {
            margin: '12px 0',
          },
        },
      },
    },
    MuiAccordionDetails: {
      styleOverrides: {
        root: {
          backgroundColor: '#FFFFFF',
          color: onBackgroundDark,
          padding: '16px',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: 'rgba(255, 178, 185, 0.08)',
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        select: {
          backgroundColor: surfaceDark,
          color: onSurfaceDark,
          '&:focus': {
            backgroundColor: surfaceDark,
          },
        },
        icon: {
          color: primaryDark,
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: primaryDark,
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: '#FF9BA4',
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: primaryDark,
          },
        },
      },
    },
  },
}); 